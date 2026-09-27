# The talk: "Spring is not magic"

`minispring-talk.pptx` — 39 slides, built for **45 minutes**, with speaker notes on every slide.

The thread running through it: *an annotation does nothing; something has to read it; that something is a few hundred readable lines.*

## Running order

| Part | Slides | Minutes | What the audience takes away |
|---|---|---|---|
| Hook | 1–3 | 3 | "I am finally going to understand @Autowired" |
| 1. Spring, then Spring Boot | 4–8 | 8 | WAR or JAR: who owns the server |
| 2. Why injection, and the minimum a container needs | 9–13 | 7 | who chooses the implementation, and the rules that follow |
| 3. What an annotation really is | 14–20 | 12 | **the heart** — bytecode, javap, reflection |
| 4. Every annotation, and who reads it | 21–30 | 16 | the mapping table, the self-invocation bug, JDK proxies against CGLIB |
| 5. Invent your own annotation | 31–32 | 8 | "I can write @Timed myself" |
| 6. The Boot layer | 33–36 | 6 | how @Conditional decides |
| 7. What it costs | 37–38 | 3 | honesty about the numbers |
| 8. Takeaways | 39 | 2 | the repository, commit by commit |

**Shorter versions.** For a 20-minute slot, keep the hook, the two slides on why injection exists, part 3 and part 5, and finish on the takeaways. For a 90-minute workshop, keep everything and let the room write `@Timed` themselves.

## Two audiences, one deck

Every section follows the same three beats: the idea in one sentence, then ten lines of code, then the edge case. Announce early that the deep dives are optional, so beginners relax and experts wait for their reward. The speaker notes mark those moments.

## The two demos

1. **javap** (slide 17). Compile a `@Component` class, run `javap -v`, point at `RuntimeVisibleAnnotations`. Change the retention to `CLASS`, recompile, run again: the attribute is now `RuntimeInvisibleAnnotations`, and the application starts with no beans. This is the moment the room gets it.
2. **The three scenarios** (slide 36). `./mvnw -q -pl demo-autoconfigure -am compile exec:exec` — the starter's default, the application's own bean, and the starter switched off with its report.

Record both beforehand. A demo that fails live costs twenty minutes.

## Rebuilding the deck

```bash
node docs/presentation/build-deck.js      # needs: npm install pptxgenjs
```

Diagrams are plain HTML in `diagrams/`, rendered to PNG at 1600×900:

```bash
msedge --headless --window-size=1600,900 --screenshot=d1-lifecycle.png d1-lifecycle.html
```

To edit a diagram by hand instead, open [excalidraw.com](https://excalidraw.com), drop the PNG in as a reference and redraw it — the palette in `style.css` is Excalidraw's own, so the two styles match.
