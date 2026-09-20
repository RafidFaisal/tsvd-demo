#!/usr/bin/env bash

set -u

iterations=(10 100 1000 10000)
testNumber=5
timeout_seconds=180
output="benchmark-results.csv"
total_tests=$(( ${#iterations[@]} * 2 * testNumber ))
completed_tests=0

printf 'iterations,mode,runs,completed_runs,avg_elapsed_millis,avg_delay_injections,conflict_detected\n' > "$output"

for count in "${iterations[@]}"; do
    for mode in baseline tsvd4j; do
        elapsed_sum=0
        delay_sum=0
        completed_runs=0
        conflict_detected=false

        for ((run=1; run<=testNumber; run++)); do
            log_file="$(mktemp)"
            run_status=failed
            elapsed=""

            if [[ "$mode" == "baseline" ]]; then
                command=(mvn -q
                    "-Dbenchmark.iterations=$count"
                    -Dtest=org.apache.log4j.helpers.AppenderAttachableTest
                    test)
            else
                rm -rf .tsvd4j
                command=(mvn -q
                    "-Dbenchmark.iterations=$count"
                    -Dtest=org.apache.log4j.helpers.AppenderAttachableTest
                    tsvd4j:tsvd4j)
            fi

            if timeout "${timeout_seconds}s" "${command[@]}" >"$log_file" 2>&1; then
                elapsed="$(sed -n \
                    's/.*BENCHMARK iterations=[0-9]* elapsedMillis=\([0-9]*\) completed=.*/\1/p' \
                    "$log_file" | tail -n 1)"

                if [[ -n "$elapsed" ]]; then
                    elapsed_sum=$((elapsed_sum + elapsed))
                    completed_runs=$((completed_runs + 1))
                    run_status=completed
                fi
            fi

            completed_tests=$((completed_tests + 1))
            if [[ "$run_status" == "completed" ]]; then
                printf '\rRunning %s for %s iterations: (%d/%d)...' \
                    "$mode" "$count" "$completed_tests" "$total_tests"
            else
                printf '\rRunning %s for %s iterations: (%d/%d)... [%s]' \
                    "$mode" "$count" "$completed_tests" "$total_tests" "$run_status"
            fi

            if [[ "$mode" == "tsvd4j" ]]; then
                delay_count="$(sed -n \
                    's/TSVD4J_DELAY_INJECTIONS = \([0-9]*\)/\1/p' \
                    "$log_file" | tail -n 1)"

                if [[ -n "$delay_count" ]]; then
                    delay_sum=$((delay_sum + delay_count))
                fi

                if [[ -s .tsvd4j/Conflicting-Pairs.txt ]]; then
                    conflict_detected=true
                fi
            fi

            rm -f "$log_file"
        done

        printf '\n'

        if (( completed_runs > 0 )); then
            avg_elapsed=$((elapsed_sum / completed_runs))
        else
            avg_elapsed=""
        fi

        if [[ "$mode" == "tsvd4j" && $completed_runs -gt 0 ]]; then
            avg_delay=$((delay_sum / completed_runs))
        else
            avg_delay=0
        fi

        printf '%s,%s,%s,%s,%s,%s,%s\n' \
            "$count" \
            "$mode" \
            "$testNumber" \
            "$completed_runs" \
            "$avg_elapsed" \
            "$avg_delay" \
            "$conflict_detected" >> "$output"
    done
done

cat "$output"