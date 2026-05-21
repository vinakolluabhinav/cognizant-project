#!/usr/bin/env python3
"""
DepositCoreX — Full Stack Launcher
===================================
Starts every microservice + React frontend with a single command.

Usage
-----
  python start.py                  # start all (existing jars)
  python start.py --build          # rebuild ALL jars first, then start
  python start.py --no-frontend    # skip the React dev server
  python start.py --build --no-frontend
  python start.py --stop           # kill any running instances on known ports

Requirements: Java 21+, Maven, Node 18+, Python 3.8+
"""

import os
import sys
import time
import socket
import signal
import argparse
import subprocess
import threading
from pathlib import Path
from datetime import datetime

# ── Terminal colours ──────────────────────────────────────────────────────────
WIN = sys.platform == "win32"

def _c(code): return f"\033[{code}m" if not WIN or _ansi_ok() else ""
def _ansi_ok():
    try:
        import ctypes
        k = ctypes.windll.kernel32
        k.SetConsoleMode(k.GetStdHandle(-11), 7)
        return True
    except Exception:
        return False

BOLD  = _c("1");  RESET = _c("0")
GREEN = _c("92"); YELLOW = _c("93"); RED = _c("91")
CYAN  = _c("96"); BLUE   = _c("94"); GRAY = _c("90")
WHITE = _c("97")

# ── Paths ─────────────────────────────────────────────────────────────────────
BASE = Path(__file__).parent.resolve()
LOGS = BASE / "logs"
LOGS.mkdir(exist_ok=True)

# ── Service catalogue ─────────────────────────────────────────────────────────
#   name         : display label
#   dir          : path relative to BASE
#   jar          : jar filename (inside <dir>/target/)
#   port         : HTTP port the service binds to
#   wait_ready   : True = block until port is open before launching next batch
#   startup_wait : extra seconds to sleep after the port opens (allow Eureka reg)
SERVICES = [
    dict(name="Eureka Server",          dir="eureka-server",
         jar="eureka-server-0.0.1-SNAPSHOT.jar",                 port=8761, wait_ready=True,  startup_wait=6),
    dict(name="API Gateway",            dir="api-gateway",
         jar="api-gateway-0.0.1-SNAPSHOT.jar",                   port=8079, wait_ready=True,  startup_wait=3),
    dict(name="IAM Service",            dir="Module_2.1/iam-service",
         jar="iam-service-0.0.1-SNAPSHOT.jar",                   port=8081, wait_ready=False, startup_wait=0),
    dict(name="Customer Onboarding",    dir="Module_2.2/customer-onboarding-service",
         jar="customer-onboarding-service-0.0.1-SNAPSHOT.jar",   port=8082, wait_ready=False, startup_wait=0),
    dict(name="CASA Service",           dir="Module_2.3/CASA",
         jar="casa-service-0.0.1-SNAPSHOT.jar",                  port=8083, wait_ready=False, startup_wait=0),
    dict(name="Product Config",         dir="Module_2.4/DepositCoreX",
         jar="product-config-service-0.0.1-SNAPSHOT.jar",        port=8084, wait_ready=False, startup_wait=0),
    dict(name="Transaction Service",    dir="Module_2.5/Transaction",
         jar="transaction-service-0.0.1-SNAPSHOT.jar",           port=8085, wait_ready=False, startup_wait=0),
    dict(name="Interest Service",       dir="Module_2.6/interest-service",
         jar="interest-service-0.0.1-SNAPSHOT.jar",              port=8086, wait_ready=False, startup_wait=0),
    dict(name="Hold & Lien Service",    dir="Module_2.7/Hold&Lien SI",
         jar="hold-lien-service-0.0.1-SNAPSHOT.jar",             port=8087, wait_ready=False, startup_wait=0),
    dict(name="TD Servicing",           dir="Module_2.8/td-servicing-service",
         jar="td-servicing-service-0.0.1-SNAPSHOT.jar",          port=8088, wait_ready=False, startup_wait=0),
    dict(name="Statements Service",     dir="Module_2.9/statements-service",
         jar="statements-service-0.0.1-SNAPSHOT.jar",            port=8089, wait_ready=False, startup_wait=0),
    dict(name="Notifications Service",  dir="Module_2.10/notifications-service",
         jar="notifications-service-0.0.1-SNAPSHOT.jar",         port=8090, wait_ready=False, startup_wait=0),
]

FRONTEND = dict(name="React Frontend", dir="frontend", port=3000)

# ── Helpers ───────────────────────────────────────────────────────────────────
_lock = threading.Lock()

def log(msg, color=WHITE):
    ts = datetime.now().strftime("%H:%M:%S")
    with _lock:
        print(f"{GRAY}[{ts}]{RESET} {color}{msg}{RESET}", flush=True)

def banner():
    print(f"""
{CYAN}{BOLD}╔══════════════════════════════════════════════════════╗
║        DepositCoreX  —  Full Stack Launcher          ║
╚══════════════════════════════════════════════════════╝{RESET}
  Base : {BASE}
  Logs : {LOGS}
""")

def port_open(port: int, host="127.0.0.1", timeout=1.0) -> bool:
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except OSError:
        return False

def wait_for_port(name: str, port: int, timeout=120) -> bool:
    log(f"⏳  Waiting for {name} on port {port}…", YELLOW)
    deadline = time.time() + timeout
    while time.time() < deadline:
        if port_open(port):
            log(f"✅  {name} is UP  (port {port})", GREEN)
            return True
        time.sleep(2)
    log(f"❌  Timeout waiting for {name} (port {port})", RED)
    return False

def mvn_cmd():
    """Pick mvn or mvn.cmd depending on OS."""
    return "mvn.cmd" if WIN else "mvn"

# ── Build ─────────────────────────────────────────────────────────────────────
def build_service(svc: dict) -> bool:
    svc_dir = BASE / svc["dir"]
    log(f"🔨  Building  {svc['name']} …", CYAN)
    log_path = LOGS / f"build-{svc['name'].replace(' ', '_').replace('&', 'n')}.log"

    cmd = [mvn_cmd(), "clean", "package", "-DskipTests", "--no-transfer-progress"]
    try:
        with open(log_path, "w") as lf:
            result = subprocess.run(
                cmd, cwd=str(svc_dir),
                stdout=lf, stderr=subprocess.STDOUT,
                timeout=300
            )
        if result.returncode == 0:
            log(f"✅  Built     {svc['name']}", GREEN)
            return True
        else:
            log(f"❌  Build FAILED for {svc['name']}  (see {log_path})", RED)
            return False
    except subprocess.TimeoutExpired:
        log(f"❌  Build TIMEOUT for {svc['name']}", RED)
        return False
    except FileNotFoundError:
        log("❌  Maven not found — make sure 'mvn' is on your PATH", RED)
        return False

def build_all():
    log("━━━  BUILD PHASE  ━━━", CYAN)
    ok = True
    for svc in SERVICES:
        if not build_service(svc):
            ok = False
    log("━━━  BUILD COMPLETE  ━━━", CYAN if ok else RED)
    return ok

# ── Launch ────────────────────────────────────────────────────────────────────
processes: list[subprocess.Popen] = []

def _popen_flags():
    """Extra kwargs for Popen on Windows to allow clean termination."""
    if WIN:
        return {"creationflags": subprocess.CREATE_NEW_PROCESS_GROUP}
    return {}

def start_service(svc: dict) -> subprocess.Popen | None:
    svc_dir  = BASE / svc["dir"]
    jar_path = svc_dir / "target" / svc["jar"]
    log_path = LOGS / f"{svc['name'].replace(' ', '_').replace('&', 'n')}.log"

    if not jar_path.exists():
        log(f"⚠️   JAR not found: {jar_path}", YELLOW)
        log(f"    Run with --build first, or build manually.", YELLOW)
        return None

    log(f"🚀  Starting  {svc['name']}  (port {svc['port']})", BLUE)
    lf = open(log_path, "w")
    proc = subprocess.Popen(
        ["java", "-jar", str(jar_path)],
        cwd=str(svc_dir),
        stdout=lf, stderr=subprocess.STDOUT,
        **_popen_flags()
    )
    processes.append(proc)
    return proc

def start_frontend(no_frontend: bool) -> subprocess.Popen | None:
    if no_frontend:
        return None
    fe_dir   = BASE / FRONTEND["dir"]
    log_path = LOGS / "React_Frontend.log"

    if not (fe_dir / "package.json").exists():
        log("⚠️   frontend/package.json not found — skipping", YELLOW)
        return None

    log(f"🌐  Starting  React Frontend  (port {FRONTEND['port']})", BLUE)
    npm = "npm.cmd" if WIN else "npm"
    lf  = open(log_path, "w")
    proc = subprocess.Popen(
        [npm, "run", "dev"],
        cwd=str(fe_dir),
        stdout=lf, stderr=subprocess.STDOUT,
        **_popen_flags()
    )
    processes.append(proc)
    return proc

# ── Shutdown ──────────────────────────────────────────────────────────────────
def shutdown(signum=None, frame=None):
    print()
    log("⛔  Shutting down all services…", YELLOW)
    for proc in processes:
        try:
            if WIN:
                proc.send_signal(signal.CTRL_BREAK_EVENT)
            else:
                proc.terminate()
        except Exception:
            pass

    time.sleep(3)

    for proc in processes:
        if proc.poll() is None:
            try:
                proc.kill()
            except Exception:
                pass

    log("👋  All services stopped. Goodbye!", CYAN)
    sys.exit(0)

# ── Stop mode — kill any process on known ports ───────────────────────────────
def stop_all():
    log("⛔  Stopping existing processes on known ports…", YELLOW)
    all_ports = [s["port"] for s in SERVICES] + [FRONTEND["port"]]
    killed = 0
    for port in all_ports:
        if WIN:
            r = subprocess.run(
                f"for /f \"tokens=5\" %a in ('netstat -ano ^| findstr :{port}') do taskkill /F /PID %a",
                shell=True, capture_output=True
            )
            if r.returncode == 0:
                log(f"  Killed process on port {port}", GREEN)
                killed += 1
        else:
            r = subprocess.run(f"lsof -ti:{port} | xargs kill -9 2>/dev/null", shell=True)
            if r.returncode == 0:
                killed += 1
    log(f"Done — {killed} process(es) stopped.", GREEN if killed else GRAY)

# ── Status printer ────────────────────────────────────────────────────────────
def status_loop(stop_event: threading.Event):
    """Periodically print which services are UP/DOWN."""
    time.sleep(15)   # initial quiet period while services boot
    while not stop_event.is_set():
        lines = [f"\n{BOLD}{'─'*54}{RESET}"]
        lines.append(f"  {'Service':<28} {'Port':>5}  {'Status'}")
        lines.append(f"  {'─'*28}  {'─'*5}  {'─'*6}")
        for svc in SERVICES:
            up = port_open(svc["port"])
            badge = f"{GREEN}● UP  {RESET}" if up else f"{RED}○ DOWN{RESET}"
            lines.append(f"  {svc['name']:<28} {svc['port']:>5}  {badge}")
        # Frontend
        up = port_open(FRONTEND["port"])
        badge = f"{GREEN}● UP  {RESET}" if up else f"{RED}○ DOWN{RESET}"
        lines.append(f"  {FRONTEND['name']:<28} {FRONTEND['port']:>5}  {badge}")
        lines.append(f"{BOLD}{'─'*54}{RESET}")
        lines.append(f"  {GRAY}Logs → {LOGS}   |   Ctrl+C to stop{RESET}\n")
        with _lock:
            print("\n".join(lines), flush=True)
        stop_event.wait(30)

# ── Main ──────────────────────────────────────────────────────────────────────
def main():
    parser = argparse.ArgumentParser(description="DepositCoreX launcher")
    parser.add_argument("--build",        action="store_true", help="Rebuild all JARs before starting")
    parser.add_argument("--no-frontend",  action="store_true", help="Skip React frontend")
    parser.add_argument("--stop",         action="store_true", help="Kill processes on known ports and exit")
    args = parser.parse_args()

    banner()

    # ── Stop mode ────────────────────────────────────────────────────────────
    if args.stop:
        stop_all()
        return

    # ── Register Ctrl+C handler ───────────────────────────────────────────────
    signal.signal(signal.SIGINT,  shutdown)
    if not WIN:
        signal.signal(signal.SIGTERM, shutdown)

    # ── Optional build phase ─────────────────────────────────────────────────
    if args.build:
        ok = build_all()
        if not ok:
            log("⚠️   Some builds failed. Attempting to start anyway…", YELLOW)
        print()

    # ── Launch phase ─────────────────────────────────────────────────────────
    log("━━━  LAUNCH PHASE  ━━━", CYAN)

    for svc in SERVICES:
        proc = start_service(svc)

        if svc["wait_ready"] and proc is not None:
            ok = wait_for_port(svc["name"], svc["port"], timeout=120)
            if not ok:
                log(f"⚠️   {svc['name']} did not come up — continuing anyway", YELLOW)
            elif svc["startup_wait"] > 0:
                log(f"⏱️   Extra {svc['startup_wait']}s for {svc['name']} to register with Eureka…", GRAY)
                time.sleep(svc["startup_wait"])

    # ── Start remaining services (non-wait) already launched above ────────────
    # Frontend last
    start_frontend(args.no_frontend)

    print()
    log("━━━  ALL SERVICES LAUNCHED  ━━━", GREEN)
    log(f"🌍  Frontend  →  http://localhost:{FRONTEND['port']}", CYAN)
    log(f"📋  Eureka    →  http://localhost:8761", CYAN)
    log(f"🔀  Gateway   →  http://localhost:8079", CYAN)
    log(f"📁  Log files →  {LOGS}", GRAY)
    log("Press  Ctrl+C  to stop everything.\n", YELLOW)

    # ── Background status thread ──────────────────────────────────────────────
    stop_event = threading.Event()
    status_thread = threading.Thread(target=status_loop, args=(stop_event,), daemon=True)
    status_thread.start()

    # ── Keep alive — wait for any process to exit unexpectedly ───────────────
    try:
        while True:
            for proc in processes:
                rc = proc.poll()
                if rc is not None:
                    # Find which service this was
                    pass   # logged already; keep running others
            time.sleep(5)
    except KeyboardInterrupt:
        stop_event.set()
        shutdown()


if __name__ == "__main__":
    main()
