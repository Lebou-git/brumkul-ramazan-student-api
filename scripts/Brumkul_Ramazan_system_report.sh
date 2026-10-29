#!/bin/bash

set -u

STUDENT_FULL_NAME="${STUDENT_FULL_NAME:-Ramazan Brumkul}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
LOG_FILE="${1:-$PROJECT_DIR/logs/Brumkul_Ramazan_system_report.log}"
OS_TYPE="$(uname -s)"

to_gb() {
    awk -v bytes="$1" 'BEGIN { printf "%.1f GB", bytes / 1024 / 1024 / 1024 }'
}

os_info() {
    if [ "$OS_TYPE" = "Darwin" ]; then
        echo "$(sw_vers -productName) $(sw_vers -productVersion) ($(uname -m))"
    elif [ -r /etc/os-release ]; then
        (. /etc/os-release && echo "${PRETTY_NAME:-Linux} ($(uname -m))")
    else
        echo "$OS_TYPE ($(uname -m))"
    fi
}

cpu_info() {
    if [ "$OS_TYPE" = "Darwin" ]; then
        local model cores performance efficiency
        model="$(sysctl -n machdep.cpu.brand_string 2>/dev/null)"
        cores="$(sysctl -n hw.ncpu 2>/dev/null)"
        performance="$(sysctl -n hw.perflevel0.physicalcpu 2>/dev/null)"
        efficiency="$(sysctl -n hw.perflevel1.physicalcpu 2>/dev/null)"
        if [ -n "$performance" ] && [ -n "$efficiency" ]; then
            echo "$model, $cores cores ($performance performance + $efficiency efficiency)"
        else
            echo "$model, $cores cores"
        fi
    else
        local model
        model="$(lscpu 2>/dev/null | awk -F: '/^Model name/ { sub(/^[ \t]+/, "", $2); print $2; exit }')"
        [ -z "$model" ] && model="$(awk -F: '/^model name/ { sub(/^[ \t]+/, "", $2); print $2; exit }' /proc/cpuinfo 2>/dev/null)"
        echo "${model:-unknown CPU}, $(nproc 2>/dev/null || echo "?") cores"
    fi
}

cpu_load() {
    uptime | sed 's/.*load average[s]*: //'
}

memory_info() {
    if [ "$OS_TYPE" = "Darwin" ]; then
        local total page_size used
        total="$(sysctl -n hw.memsize)"
        page_size="$(sysctl -n hw.pagesize)"
        used="$(vm_stat | awk -v size="$page_size" '
            /Pages active/                 { active = $3 + 0 }
            /Pages wired down/             { wired = $4 + 0 }
            /Pages occupied by compressor/ { compressed = $5 + 0 }
            END { printf "%.0f", (active + wired + compressed) * size }')"
        echo "total $(to_gb "$total"), used $(to_gb "$used"), available $(to_gb "$((total - used))")"
    elif command -v free >/dev/null 2>&1; then
        free -h | awk '/^Mem:/ { print "total " $2 ", used " $3 ", available " $7 }'
    else
        awk '/^MemTotal/ { t = $2 } /^MemAvailable/ { a = $2 }
             END { printf "total %.1f GB, used %.1f GB, available %.1f GB\n", t/1048576, (t-a)/1048576, a/1048576 }' /proc/meminfo
    fi
}

disk_line() {
    if [ "$OS_TYPE" = "Darwin" ]; then
        df -h "$HOME" | awk 'NR == 2'
    else
        df -Ph "$HOME" | awk 'NR == 2'
    fi
}

mkdir -p "$(dirname "$LOG_FILE")"
DISK="$(disk_line)"

{
    echo "=================================================="
    echo " System report of $STUDENT_FULL_NAME"
    echo "=================================================="
    echo "Student full name: $STUDENT_FULL_NAME"
    echo "Date and time: $(date '+%Y-%m-%d %H:%M:%S %Z')"
    echo "Username: $(whoami)"
    echo "Hostname: $(hostname)"
    echo "Operating system: $(os_info)"
    echo "Kernel: $(uname -s) $(uname -r)"
    echo "CPU: $(cpu_info)"
    echo "CPU load average (1, 5, 15 min): $(cpu_load)"
    echo "Memory: $(memory_info)"
    echo "Disk usage ($(echo "$DISK" | awk '{ print $NF }')): $(echo "$DISK" | awk '{ print $3 " used of " $2 " (" $5 ")" }')"
    echo "Available disk space: $(echo "$DISK" | awk '{ print $4 }')"
    echo "Project size: $(du -sh "$PROJECT_DIR" 2>/dev/null | awk '{ print $1 }')"
    echo "=================================================="
} | tee -a "$LOG_FILE"

echo "Report of $STUDENT_FULL_NAME saved to $LOG_FILE"
