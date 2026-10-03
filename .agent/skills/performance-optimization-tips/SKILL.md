---
name: performance-optimization-tips
description: Optimizes software performance across execution speed, memory footprint, and architectural scalability. Identifies CPU bottlenecks, inefficient algorithms, excessive allocations, memory leaks, and unnecessary UI rendering or recompositions, delivering benchmarkable, production-ready code. Use this skill when diagnosing slowness, high memory consumption, frame drops, or when profiling performance-critical code paths.
---
# Performance Optimization Tips

This skill provides an engineering framework to diagnose, analyze, and eliminate performance bottlenecks. It focuses on maximizing execution speed, minimizing memory consumption, and ensuring sustainable scalability while preserving code readability and correctness.

## Core Engineering Goals
1. **Speed (Execution Latency & Throughput)**: Minimize CPU cycles, eliminate thread blocking on latency-critical threads, optimize algorithmic complexity.
2. **Memory Usage (Allocations & Retention)**: Eliminate GC pressure caused by short-lived allocations in hot paths, resolve memory leaks and unbounded collections.
3. **Scalability (Load & Volume Growth)**: Ensure $O(1)$ or $O(n \log n)$ scaling, implement pagination/caching, and prevent cascading queries.

## Step-by-Step Optimization Workflow
1. **Profile & Isolate the Hotspot**: Determine if the bottleneck is CPU, Memory, I/O, or UI render-bound.
2. **Evaluate Invariants & Algorithmic Complexity**: Audit time and space complexity ($O(N)$). Check if work can be avoided or cached.
3. **Eliminate Waste & Optimize Allocations**: Reuse instances, pool allocations, and stream data in single-pass operations.
4. **Isolate & Stabilize UI Rendering**: Stabilize parameters, isolate fast-changing state to leaf nodes, avoid re-rendering.
5. **Verify Correctness & Benchmark**: Ensure invariants hold and compare before vs. after metrics.

## Standard Output Format
When responding, structure your output strictly into:
### 1. Performance Issues (Bottlenecks, Inefficient Logic, Unnecessary Rendering)
### 2. Optimization Strategies (Technical Rationale, Expected Gains, Trade-offs)
### 3. Improved Code (Production-Ready Code, In-Code Documentation, Verification Guidelines)