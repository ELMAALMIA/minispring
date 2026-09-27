# The talk: "Spring is not magic"

Two sessions of 45 minutes, each with speaker notes on every slide, each able to stand alone.

| File | Slides | Promise |
|---|---|---|
| `minispring-talk-1-beans.pptx` | 26 | *By the end, you know exactly what happens between `@Component` and your object.* |
| `minispring-talk-2-proxies.pptx` | 21 | *By the end, you know why your `@Transactional` sometimes does nothing.* |

A PDF of each sits next to it, for sharing. The thread running through both: *an annotation does nothing; something has to read it; that something is a few hundred readable lines.*

## Part 1 — How a bean is born

| Section | Slides | Minutes | What the audience takes away |
|---|---|---|---|
| Title, agenda, hook | 1–3 | 3 | "I am finally going to understand @Autowired" |
| 1. Spring, then Spring Boot | 4–6 | 6 | WAR or JAR: who owns the server |
| 2. Why injection at all | 7–10 | 7 | the class no longer chooses its collaborators |
| 3. What an annotation really is | 11–16 | 12 | **the heart** — the `javap` demo |
| 4. Who reads which annotation | 17–25 | 15 | the mapping table, scopes, lifecycle, cycles |
| Takeaways | 26 | 2 | the repository, and what part 2 covers |

## Part 2 — What happens to your bean

| Section | Slides | Minutes | What the audience takes away |
|---|---|---|---|
| Title, recap of part 1 | 1–3 | 4 | enough to follow without having seen part 1 |
| 1. The seam | 4–6 | 5 | a post-processor may hand you a different object |
| 2. Proxies | 7–10 | 12 | **the self-invocation bug**, and CGLIB |
| 3. Invent your own annotation | 11–12 | 8 | "I can write @Timed myself" |
| 4. Auto-configuration | 13–17 | 10 | how `@Conditional` decides, and the report |
| 5. What it costs, takeaways | 18–21 | 5 | honest numbers, then the repository |

## How to give them

- **Two minutes per slide.** That is the pace both decks are built for. A code slide takes two minutes, a table takes three.
- **Slides marked `[CUT IF LATE]` in the notes** can be dropped live without breaking anything: three in part 1, two in part 2.
- **Two planned questions per deck**, in the notes: "who has written Spring in XML?" before the XML slide, and "who has lost time on a `@Transactional` that did nothing?" before the self-invocation slide. They give the room a break, and you thirty seconds.
- **Slides in English, spoken in whatever language the room prefers.** That keeps the decks usable for an international CFP.

## The three demos

1. **javap** (part 1, slide 14). Compile a `@Component` class, run `javap -v`, point at `RuntimeVisibleAnnotations`. Change the retention to `CLASS`, recompile, run again: the attribute is now `RuntimeInvisibleAnnotations`, and the application starts with no beans.
2. **`@Timed` live** (part 2, slide 12). Copy `TransactionalProcessor`, rename, run the test. Five minutes, rehearsed.
3. **The three scenarios** (part 2, slide 17). `./mvnw -q -pl demo-autoconfigure -am compile exec:exec` — the starter's default, the application's own bean, and the starter switched off with its report.

Record all three beforehand. A demo that fails live costs twenty minutes.

## Abstracts, ready to submit

**Spring is not magic (1/2): how a bean is born** — `@Autowired` is not magic, it is about 1,200 lines of Java. We rebuild the core of a dependency injection container from scratch — component scanning, constructor injection, scopes and lifecycle — and use it to explain what Spring really does at startup. We begin where nobody begins: what an annotation actually is inside the class file, proved live with `javap`. You will leave able to read Spring's own source, and knowing why a circular dependency cannot be constructed. *Audience: Java developers who use Spring. No knowledge of the internals required.*

**Spring is not magic (2/2): what happens to your bean** — Your bean is built, and then the container hands you something else. This session covers the second half of the machinery: bean post-processors, JDK and CGLIB proxies, why `@Transactional` silently does nothing on a self-invocation, and how `@Conditional` decides what auto-configuration wires up. Everything runs on a 1,500-line container with zero dependencies, and every claim has a test behind it. You will leave able to answer "why is my bean not there?" with a printed report instead of a guess. *Audience: Java developers who use Spring Boot. Part 1 is recapped in two slides, so it is not a prerequisite.*

## Rebuilding

```bash
npm install pptxgenjs
node docs/presentation/build-decks.js
```

Diagrams are plain HTML in `diagrams/`, rendered to PNG at 1600×900:

```bash
msedge --headless --window-size=1600,900 --screenshot=d1-lifecycle.png d1-lifecycle.html
```

To redraw one by hand, open [excalidraw.com](https://excalidraw.com) and use the PNG as a reference: the palette in `style.css` is Excalidraw's own, so the two styles match.
