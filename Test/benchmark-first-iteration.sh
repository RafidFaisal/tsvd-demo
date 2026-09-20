#!/usr/bin/env bash

set -u

iterations=(10 100 1000 10000)
testNumber=5
timeout_seconds=180
output="benchmark-results.csv"
total_tests=$(( ${#iterations[@]} * testNumber ))
completed_tests=0

printf 'iterations,runs,completed_runs,avg_elapsed_millis,avg_delay_injections,conflict_detected\n' > "$output"

for count in "${iterations[@]}"; do
    elapsed_sum=0
    delay_sum=0
    completed_runs=0
    conflict_detected=false

    for ((run=1; run<=testNumber; run++)); do
        log_file="$(mktemp)"
        rm -rf .tsvd4j

        if timeout "${timeout_seconds}s" mvn -q \
            -Dbenchmark.iterations="$count" \
            -Dtest=org.apache.log4j.helpers.AppenderAttachableTest \
            tsvd4j:tsvd4j >"$log_file" 2>&1; then
            elapsed="$(sed -n 's/.*BENCHMARK iterations=[0-9]* elapsedMillis=\([0-9]*\) completed=.*/\1/p' "$log_file" | tail -n 1)"
            delay_count="$(sed -n 's/TSVD4J_DELAY_INJECTIONS = \([0-9]*\)/\1/p' "$log_file" | tail -n 1)"

            if [[ -n "$elapsed" ]]; then
                elapsed_sum=$((elapsed_sum + elapsed))
                delay_sum=$((delay_sum + ${delay_count:-0}))
                completed_runs=$((completed_runs + 1))
            fi
        fi

        if [[ -s .tsvd4j/Conflicting-Pairs.txt ]]; then
            conflict_detected=true
        fi

        completed_tests=$((completed_tests + 1))
        printf '\rRunning first-iteration TSVD4J for %s iterations: (%d/%d)...' \
            "$count" "$completed_tests" "$total_tests"
        rm -f "$log_file"
    done

    printf '\n'
    if (( completed_runs > 0 )); then
        avg_elapsed=$((elapsed_sum / completed_runs))
        avg_delay=$((delay_sum / completed_runs))
    else
        avg_elapsed=""
        avg_delay=""
    fi

    printf '%s,%s,%s,%s,%s,%s\n' \
        "$count" "$testNumber" "$completed_runs" "$avg_elapsed" "$avg_delay" "$conflict_detected" >> "$output"
done

cat "$output"
