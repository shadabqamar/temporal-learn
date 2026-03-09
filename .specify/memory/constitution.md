# Constitution

> Supreme law for `temporal-user-app`.
> All plans, tasks, and implementation MUST comply with every Article.
> A ❌ violation in any plan MUST be resolved before implementation begins.

---

## Article I — Technology Stack

The project MUST use:
- **Language**: Java 17
- **Build**: Gradle 9.4 (Groovy DSL)
- **Temporal SDK**: `io.temporal:temporal-sdk:1.33.0`
- **Temporal Testing**: `io.temporal:temporal-testing:1.33.0`
- **Logging**: Logback (SLF4J) 1.5.1
- **Testing**: JUnit 5 (5.11.3) + Mockito (5.11.0) + AssertJ (3.26.0)
- **Local Temporal server**: Temporal CLI (`temporal server start-dev`)

No substitutions without amending this Article first.

---

## Article II — Temporal Architecture Rules

All Temporal code MUST follow these structural rules:

| Rule | Requirement |
|------|-------------|
| Workflow Interface | Annotated `@WorkflowInterface`; exactly ONE `@WorkflowMethod` |
| Activity Interface | Annotated `@ActivityInterface`; each method optionally `@ActivityMethod` |
| Signal Methods | Annotated `@SignalMethod` on Workflow interface |
| Query Methods | Annotated `@QueryMethod` on Workflow interface |
| Child Workflow | Started via `Workflow.newChildWorkflowStub()` — NEVER `new` |
| Activity Stub | Created via `Workflow.newActivityStub()` with `ActivityOptions` — NEVER `new` |
| Worker | Registers both Workflow and Activity types before `factory.start()` |
| Task Queue | Single constant `TASK_QUEUE = "user-onboarding-queue"` shared across all classes |

---

## Article III — Determinism (NON-NEGOTIABLE)

Workflow code MUST be deterministic. The following are BANNED inside Workflow implementations:

- `new Random()`, `Math.random()`, `UUID.randomUUID()` → use `Workflow.randomUUID()` or `Workflow.newRandom()`
- `System.currentTimeMillis()` → use `Workflow.currentTimeMillis()`
- `Thread.sleep()` → use `Workflow.sleep()`
- `new Thread()`, `ThreadPoolExecutor`, any Java concurrency primitives
- `Future` / `CompletableFuture` → use Temporal `Promise` / `CompletablePromise`
- Direct I/O, DB calls, HTTP calls — ALL side effects MUST go in Activities

---

## Article IV — Signal Design

Signals MUST be used for external input to running workflows:
- Signal handlers MUST be idempotent where possible
- Use `Workflow.await()` to block until a condition is met after receiving a signal
- Signal handler state changes MUST be reflected in `@QueryMethod` results
- Parent workflows CAN send signals to child workflows after awaiting `ChildWorkflowExecution`

---

## Article V — Child Workflow Design

Child Workflows MUST be used only when:
1. The work has unbounded Event History (long-running loops)
2. The child represents an independent unit that could run on a separate Worker
3. The child needs its own retry/timeout policies

Child Workflows MUST NOT be used purely for code organization (use Activities instead).
Always await `Async.function(child::method).getChildWorkflowExecution().get()` before sending signals.

---

## Article VI — Activity Design

- Each Activity method does ONE thing (single responsibility)
- Activities handle all I/O, external calls, DB operations
- Activity stubs MUST declare `ScheduleToCloseTimeout` or `StartToCloseTimeout`
- Activities MUST NOT call Workflow APIs
- Retry policy MUST be explicitly defined for each activity stub

---

## Article VII — Package Structure

```
com.example.temporal
├── workflow/     ← @WorkflowInterface + implementations (parent + child)
├── activity/     ← @ActivityInterface + implementations
├── worker/       ← Worker registration + factory startup
├── starter/      ← WorkflowClient entry points (start, signal, query)
└── model/        ← Plain Java data objects (no Temporal annotations)
```

---

## Article VIII — Testing Standards (NON-NEGOTIABLE)

- Use `TestWorkflowEnvironment` for all Workflow tests (NOT real Temporal server)
- Every Workflow MUST have at least one happy-path test
- Signal handling MUST be tested (send signal → verify state change)
- Child Workflow execution MUST be tested end-to-end
- Activity failures / retries MUST have at least one test
- All tests use `@DisplayName`, are independent, and use no shared mutable state

---

## Article IX — Logging & Observability

- Use SLF4J logger (`LoggerFactory.getLogger(...)`) in Activities and Workers
- Workflow code MUST NOT use loggers directly — use `Workflow.getLogger()` instead
- All workflow executions use a deterministic `workflowId` (e.g. `"user-onboarding-" + userId`)
- Search attributes or memos for important workflow metadata are encouraged but optional

---

## Article X — Simplicity Gate

- Maximum 1 Worker process (all queues registered to one Worker in dev)
- No dependency injection framework (no Spring in core workflow code)
- No premature abstraction — concrete implementations preferred over deep hierarchies
- Starter classes are plain `main()` methods — not web controllers

---

_Last updated: March 2026 | Stack: Java 17 + Temporal SDK 1.33.0 + Gradle 9.4_
