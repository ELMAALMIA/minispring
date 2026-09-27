const pptxgen = require("pptxgenjs");
const path = require("path");

const DIAGRAMS = path.join(__dirname, "diagrams");
const OUT = "C:/Users/ayoub/Desktop/spring-app/docs/presentation/minispring-talk.pptx";

const INK = "1B2430";      // dark slate, title & section slides
const INK_SOFT = "2E3A47";
const PAPER = "FFFFFF";
const TEXT = "22303C";
const MUTED = "5B6B7A";
const ACCENT = "2F9E44";   // green
const WARN = "E8590C";     // amber
const CODE_BG = "F1F3F5";
const BODY = "Calibri";
const HEAD = "Cambria";
const MONO = "Courier New";

const pres = new pptxgen();
pres.layout = "LAYOUT_16x9"; // 10 x 5.625 in
pres.author = "Ayoub EL Maalmi";
pres.title = "Spring is not magic";

/* ---------- helpers ---------- */

function light() {
  const s = pres.addSlide();
  s.background = { color: PAPER };
  return s;
}

function title(s, text, sub) {
  s.addText(text, {
    x: 0.5, y: 0.32, w: 9, h: 0.78, isTextBox: true,
    fontFace: HEAD, fontSize: text.length > 38 ? 26 : 30, bold: true, color: TEXT, valign: "top", margin: 0,
  });
  if (sub) {
    s.addText(sub, {
      x: 0.5, y: 1.08, w: 9, h: 0.32, isTextBox: true,
      fontFace: BODY, fontSize: 15, color: MUTED, margin: 0,
    });
  }
}

function section(number, text, sub, notes) {
  const s = pres.addSlide();
  s.background = { color: INK };
  s.addShape(pres.ShapeType.ellipse, {
    x: 0.9, y: 2.1, w: 1.1, h: 1.1, fill: { color: ACCENT },
  });
  s.addText(String(number), {
    x: 0.9, y: 2.1, w: 1.1, h: 1.1, isTextBox: true,
    fontFace: HEAD, fontSize: 40, bold: true, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
  });
  s.addText(text, {
    x: 2.3, y: 2.15, w: 7, h: 0.7, isTextBox: true,
    fontFace: HEAD, fontSize: 30, bold: true, color: "FFFFFF", margin: 0,
  });
  s.addText(sub, {
    x: 2.3, y: 2.85, w: 7, h: 0.5, isTextBox: true,
    fontFace: BODY, fontSize: 16, color: "C7D2DD", margin: 0,
  });
  s.addNotes(notes);
  return s;
}

function code(s, lines, opts) {
  const o = Object.assign({ x: 0.5, y: 1.6, w: 5.9, h: 2.6, size: 13 }, opts || {});
  s.addShape(pres.ShapeType.roundRect, {
    x: o.x, y: o.y, w: o.w, h: o.h, fill: { color: CODE_BG }, line: { color: "DEE2E6", width: 1 }, rectRadius: 0.06,
  });
  s.addText(lines.join("\n"), {
    x: o.x + 0.18, y: o.y + 0.12, w: o.w - 0.36, h: o.h - 0.24, isTextBox: true,
    fontFace: MONO, fontSize: o.size, color: TEXT, margin: 0, lineSpacingMultiple: 1.15,
  });
}

function bullets(s, items, opts) {
  const o = Object.assign({ x: 0.55, y: 1.7, w: 9, h: 3.2, size: 16 }, opts || {});
  s.addText(
    items.map((t, i) => ({
      text: t,
      options: { bullet: true, breakLine: i !== items.length - 1, paraSpaceAfter: 8 },
    })),
    { x: o.x, y: o.y, w: o.w, h: o.h, isTextBox: true, fontFace: BODY, fontSize: o.size, color: TEXT, margin: 0 }
  );
}

function callout(s, text, opts) {
  const o = Object.assign({ x: 0.5, y: 4.55, w: 9, h: 0.65, color: ACCENT }, opts || {});
  s.addShape(pres.ShapeType.roundRect, {
    x: o.x, y: o.y, w: o.w, h: o.h, fill: { color: "FFFFFF" }, line: { color: o.color, width: 2 }, rectRadius: 0.08,
  });
  s.addText(text, {
    x: o.x + 0.2, y: o.y, w: o.w - 0.4, h: o.h, isTextBox: true,
    fontFace: BODY, fontSize: 15, bold: true, color: o.color, valign: "middle", margin: 0,
  });
}

/** A diagram fills the slide: each one already carries its own title. */
function diagramSlide(file, notes) {
  const s = light();
  s.addImage({ path: path.join(DIAGRAMS, file), x: 0.5, y: 0.28, w: 9.0, h: 5.06 });
  s.addNotes(notes);
  return s;
}

function table(s, rows, opts) {
  const o = Object.assign({ x: 0.5, y: 1.65, w: 9, colW: null, size: 12 }, opts || {});
  s.addTable(rows, {
    x: o.x, y: o.y, w: o.w, colW: o.colW,
    fontFace: BODY, fontSize: o.size, color: TEXT, valign: "middle",
    border: { type: "solid", color: "E9ECEF", pt: 1 },
    rowH: 0.34, margin: 0.06,
  });
}

function header(cells) {
  return cells.map((t) => ({
    text: t,
    options: { bold: true, color: "FFFFFF", fill: { color: INK_SOFT }, fontSize: 12 },
  }));
}

function mono(t, size) {
  return { text: t, options: { fontFace: MONO, fontSize: size || 11 } };
}

function mono9(t) {
  return mono(t, 9);
}

/* ---------- 1. Title ---------- */
{
  const s = pres.addSlide();
  s.background = { color: INK };
  s.addText("Spring is not magic", {
    x: 0.8, y: 1.65, w: 8.5, h: 0.9, isTextBox: true,
    fontFace: HEAD, fontSize: 44, bold: true, color: "FFFFFF", margin: 0,
  });
  s.addText("I rebuilt the container in 1,200 lines of Java 21", {
    x: 0.8, y: 2.6, w: 8.5, h: 0.5, isTextBox: true,
    fontFace: BODY, fontSize: 20, color: "9FB3C8", margin: 0,
  });
  s.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 3.5, w: 4.6, h: 0.55, fill: { color: ACCENT }, rectRadius: 0.1,
  });
  s.addText("github.com/ELMAALMIA/minispring", {
    x: 0.8, y: 3.5, w: 4.6, h: 0.55, isTextBox: true,
    fontFace: MONO, fontSize: 13, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
  });
  s.addText("Ayoub EL Maalmi  ·  Java & Spring Boot", {
    x: 0.8, y: 4.35, w: 8.5, h: 0.4, isTextBox: true,
    fontFace: BODY, fontSize: 14, color: "7C93A8", margin: 0,
  });
  s.addNotes(
    "Welcome. One sentence to set the contract: by the end of this talk you will have seen every piece of machinery behind @Autowired, and it is small enough to read in an afternoon.\n\n" +
    "Say who you are in ten seconds, no more. The credibility comes from the code, not from the bio.\n\n" +
    "Timing: this deck is built for 45 minutes. Parts 3, 4 and 5 are the heart; if you run late, cut part 1 to two slides."
  );
}

/* ---------- 2. Agenda ---------- */
{
  const s = light();
  title(s, "Where we are going", "Eight steps, from what Spring is to writing your own annotation");
  const items = [
    ["1", "Spring, then Spring Boot", "WAR, JAR, who owns the server"],
    ["2", "The minimum a container needs", "a Map and a few rules"],
    ["3", "What an annotation really is", "bytecode, javap, reflection"],
    ["4", "Every annotation, and who reads it", "minispring vs Spring vs XML"],
    ["5", "Invent your own annotation", "@Timed in 30 lines"],
    ["6", "The Boot layer", "auto-configuration and @Conditional"],
    ["7", "What it costs", "honest numbers"],
    ["8", "Takeaways", ""],
  ];
  items.forEach((it, i) => {
    const x = i < 4 ? 0.55 : 5.15;
    const y = 1.65 + (i % 4) * 0.87;
    s.addShape(pres.ShapeType.ellipse, { x, y, w: 0.42, h: 0.42, fill: { color: i < 4 ? ACCENT : INK_SOFT } });
    s.addText(it[0], {
      x, y, w: 0.42, h: 0.42, isTextBox: true,
      fontFace: BODY, fontSize: 14, bold: true, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
    });
    s.addText(it[1], {
      x: x + 0.58, y: y - 0.04, w: 3.7, h: 0.3, isTextBox: true,
      fontFace: BODY, fontSize: 15, bold: true, color: TEXT, margin: 0,
    });
    s.addText(it[2], {
      x: x + 0.58, y: y + 0.24, w: 3.7, h: 0.3, isTextBox: true,
      fontFace: BODY, fontSize: 12, color: MUTED, margin: 0,
    });
  });
  s.addNotes(
    "Do not read the agenda out loud, it is boring. Point at three things only: the annotation part, the mapping table, and the moment where we write our own annotation.\n\n" +
    "Tell the audience about the lightning symbol: slides marked with a bolt are deep dives for people who already know Spring well. Beginners can let those wash over them. This is what lets one talk serve two audiences."
  );
}

/* ---------- 3. Hook ---------- */
{
  const s = light();
  title(s, "You have written this a thousand times", null);
  code(s, [
    "@Service",
    "public class OrderService {",
    "",
    "    @Autowired",
    "    private OrderRepository repository;",
    "}",
  ], { x: 0.5, y: 1.5, w: 5.2, h: 2.2, size: 15 });
  s.addText("Who creates this object?", {
    x: 6.0, y: 1.7, w: 3.6, h: 0.5, isTextBox: true,
    fontFace: HEAD, fontSize: 22, bold: true, color: TEXT, margin: 0,
  });
  s.addText("And how does it know what to put inside?", {
    x: 6.0, y: 2.25, w: 3.6, h: 0.8, isTextBox: true,
    fontFace: BODY, fontSize: 16, color: MUTED, margin: 0,
  });
  callout(s, "Today we read the answer, line by line. It is not magic, it is about 1,200 lines.", { y: 4.3 });
  s.addNotes(
    "Ask the room: who here uses @Autowired every day? Most hands go up. Then: who has read the class that processes it? Almost none.\n\n" +
    "That gap is the whole talk. Do not apologise for it: nobody reads AutowiredAnnotationBeanPostProcessor, because it is 700 lines of edge cases. Mine is 40.\n\n" +
    "Keep this slide short, thirty seconds. The energy comes from the question, not from the code."
  );
}

/* ---------- Section 1 ---------- */
section(1, "Spring, then Spring Boot", "What actually changed between the two",
  "Transition: before opening the container, we need to agree on what Spring is, and what Boot added. Five minutes, no more. Many juniors in the room have never seen a WAR file; many seniors have deployed hundreds. Serve both by framing it as a question of ownership: who starts whom.");

/* ---------- 4. Timeline ---------- */
{
  const s = light();
  title(s, "Twenty years, one model", "The source of metadata changed. The model never did.");
  const steps = [
    ["2003", "Spring 1.0", "XML: <bean>"],
    ["2007", "Spring 2.5", "@Component, @Autowired"],
    ["2009", "Spring 3.0", "@Configuration, @Bean"],
    ["2014", "Spring Boot", "auto-configuration"],
    ["2022", "Spring 6", "AOT, native images"],
  ];
  steps.forEach((st, i) => {
    const x = 0.5 + i * 1.86;
    s.addShape(pres.ShapeType.roundRect, {
      x, y: 1.9, w: 1.7, h: 1.5, fill: { color: i === 3 ? ACCENT : CODE_BG },
      line: { color: i === 3 ? ACCENT : "DEE2E6", width: 1 }, rectRadius: 0.08,
    });
    s.addText(st[0], {
      x, y: 2.0, w: 1.7, h: 0.35, isTextBox: true, fontFace: HEAD, fontSize: 17, bold: true,
      color: i === 3 ? "FFFFFF" : TEXT, align: "center", margin: 0,
    });
    s.addText(st[1], {
      x, y: 2.38, w: 1.7, h: 0.3, isTextBox: true, fontFace: BODY, fontSize: 12,
      color: i === 3 ? "E8F5E9" : MUTED, align: "center", margin: 0,
    });
    s.addText(st[2], {
      x: x + 0.08, y: 2.7, w: 1.54, h: 0.6, isTextBox: true, fontFace: MONO, fontSize: 10,
      color: i === 3 ? "FFFFFF" : TEXT, align: "center", margin: 0,
    });
  });
  callout(s, "Whatever the source — XML, annotations, Java, auto-configuration — the container sees one thing: a BeanDefinition.", { y: 3.9 });
  s.addNotes(
    "The key line is the callout, and it pays off twice later: in part 4 when we compare with XML, and in part 6 with auto-configuration.\n\n" +
    "If someone asks why XML lost: annotations put the metadata next to the code it describes. XML kept it in a file nobody opened. Both feed the same model.\n\n" +
    "Do not spend more than ninety seconds here."
  );
}

/* ---------- 5. WAR vs JAR diagram ---------- */
{
  diagramSlide("d3-war-vs-jar.png",
    "This is the slide juniors will thank you for, and the one seniors will nod along to.\n\n" +
    "Say it as a reversal: before, Tomcat started and then loaded your application. Now your application starts, and then starts Tomcat. Everything else follows from that single inversion: one app per JVM, one container image, deploy by running a process.\n\n" +
    "If you have time for one anecdote, tell the story of a shared Tomcat with five WARs where one memory leak took down all five."
  );
}

/* ---------- 6. WAR vs JAR table ---------- */
{
  const s = light();
  title(s, "The same thing, as a table", "Useful in interviews, both sides of the desk");
  table(s, [
    header(["", "WAR", "Executable JAR"]),
    ["Who starts first", "the servlet container", "your main() method"],
    ["The server is", "installed and administered", "a Maven dependency"],
    ["Entry point", mono("web.xml / ServletContainerInitializer"), mono("public static void main")],
    ["Packaging", "app.war, classes and libs", "fat jar, nested jars"],
    ["Deployment", "copy into a shared server", mono("java -jar app.jar")],
    ["Fits", "one big server, many apps", "containers, Kubernetes"],
  ], { y: 1.6, colW: [2.2, 3.4, 3.4], size: 12 });
  callout(s, "Both still run Spring. Only the bootstrap changed.", { y: 4.55 });
  s.addNotes(
    "Do not read the table. Pick two rows: the entry point row and the deployment row.\n\n" +
    "A question that often comes: can Spring Boot still produce a WAR? Yes, with the war packaging and SpringBootServletInitializer. It is used when a company mandates an application server. Say it, then move on.\n\n" +
    "Bolt for the seniors: the fat jar is not a normal jar. Boot uses a nested jar layout and its own class loader, because the JVM cannot read a jar inside a jar."
  );
}

/* ---------- 7. Where minispring sits ---------- */
{
  const s = light();
  title(s, "Where minispring sits", "The same idea, in miniature");
  bullets(s, [
    "A container: scanning, injection, scopes, lifecycle, proxies, events, conditions",
    "An embedded server, in the same spirit: the JDK's own HttpServer, on virtual threads",
    "No servlet API, no Tomcat, no XML — and zero runtime dependencies",
  ], { y: 1.55, h: 1.5, size: 16 });
  code(s, [
    "try (var context = AnnotationApplicationContext.scan(\"io.minispring.demo\")) {",
    "    var server = DispatcherServer.start(context, 8080);   // ~100 lines",
    "}",
  ], { x: 0.5, y: 3.1, w: 9, h: 1.05, size: 12 });
  callout(s, "Everything you see today runs, is tested, and is on GitHub.", { y: 4.45 });
  s.addNotes(
    "Make the promise concrete: every code snippet in this talk is copied from the repository, not written for the slides.\n\n" +
    "Mention the constraint that shaped the project: zero runtime dependencies. It forces you to understand things instead of importing them. That is also why there is no CGLIB and no ASM, which will come back twice later."
  );
}

/* ---------- Section 2 ---------- */
section(2, "The minimum a container needs", "Strip Spring down to what cannot be removed",
  "Transition: now that we know where the container sits, let us define what it must do. Five minutes. This part sets the vocabulary for the rest: definition, registry, factory, post-processor.");

/* ---------- 8. A container is a Map ---------- */
{
  const s = light();
  title(s, "A container is a Map, plus rules", null);
  s.addShape(pres.ShapeType.roundRect, {
    x: 0.5, y: 1.45, w: 4.3, h: 1.0, fill: { color: INK }, rectRadius: 0.08,
  });
  s.addText("Map<String, Object>", {
    x: 0.5, y: 1.45, w: 4.3, h: 1.0, isTextBox: true,
    fontFace: MONO, fontSize: 20, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
  });
  s.addText("The interesting part is not the Map. It is the rules that fill it:", {
    x: 5.1, y: 1.5, w: 4.4, h: 0.9, isTextBox: true, fontFace: BODY, fontSize: 15, color: MUTED, margin: 0,
  });
  bullets(s, [
    "Find the classes to manage",
    "Describe them before creating them",
    "Create each one after its dependencies",
    "Run lifecycle callbacks at the right moment",
    "Let something wrap a bean before it is handed out",
    "Decide whether a bean should exist at all",
  ], { x: 0.55, y: 2.6, w: 9, h: 1.9, size: 15 });
  s.addNotes(
    "Six rules, six sections of the rest of the talk. Say that explicitly: this list is the table of contents of the machinery.\n\n" +
    "The line that lands: Spring's DefaultListableBeanFactory is 2,000 lines, but the model underneath is a map from name to object. Everything else is rules about how entries get in there, and in which order."
  );
}

/* ---------- 9. Numbers ---------- */
{
  const s = light();
  title(s, "The whole thing, in numbers", "So you know what 'small' means here");
  const stats = [
    ["1,200", "lines for the container"],
    ["0", "runtime dependencies"],
    ["129", "tests, no mocks"],
    ["~100 ms", "startup, cold JVM"],
  ];
  stats.forEach((st, i) => {
    const x = 0.5 + i * 2.3;
    s.addShape(pres.ShapeType.roundRect, { x, y: 1.9, w: 2.05, h: 1.5, fill: { color: CODE_BG }, line: { color: "DEE2E6", width: 1 }, rectRadius: 0.08 });
    s.addText(st[0], {
      x, y: 2.05, w: 2.05, h: 0.75, isTextBox: true,
      fontFace: HEAD, fontSize: 30, bold: true, color: ACCENT, align: "center", margin: 0,
    });
    s.addText(st[1], {
      x: x + 0.1, y: 2.8, w: 1.85, h: 0.5, isTextBox: true,
      fontFace: BODY, fontSize: 12, color: MUTED, align: "center", margin: 0,
    });
  });
  callout(s, "Small enough that every claim in this talk can be checked in one afternoon.", { y: 3.9 });
  s.addNotes(
    "Use the numbers to set expectations, not to brag. The 100 ms will come back in part 7, where we explain honestly why Spring Boot takes 1.7 seconds for the same application.\n\n" +
    "If someone asks about the 129 tests: JUnit 5 and AssertJ only, no Mockito. When a test needs a mock, the design is too coupled — that rule alone improved the container's design twice."
  );
}

/* ---------- Section 3 ---------- */
section(3, "What an annotation really is", "The part nobody explains, and everything depends on",
  "Transition, and the heart of the talk. Twelve minutes. Slow down here: this is where beginners either get it or lose the thread for the next thirty minutes.\n\nThe sentence to repeat three times: an annotation does nothing. Something has to read it.");

/* ---------- 10. @Component source ---------- */
{
  const s = light();
  title(s, "An annotation is an interface with no behaviour", "The real file from the project");
  code(s, [
    "@Documented",
    "@Retention(RetentionPolicy.RUNTIME)",
    "@Target(ElementType.TYPE)",
    "public @interface Component {",
    "",
    "    String value() default \"\";",
    "}",
  ], { x: 0.5, y: 1.5, w: 5.4, h: 2.4, size: 14 });
  s.addText("Six lines.", {
    x: 6.2, y: 1.6, w: 3.4, h: 0.4, isTextBox: true, fontFace: HEAD, fontSize: 20, bold: true, color: TEXT, margin: 0,
  });
  s.addText("No code runs. No class is created. It only says: someone might be interested in this class.\n\nThe interesting part is the two lines above it.", {
    x: 6.2, y: 2.1, w: 3.4, h: 1.7, isTextBox: true, fontFace: BODY, fontSize: 14, color: MUTED, margin: 0,
  });
  callout(s, "@interface, not interface. The compiler turns it into metadata stored in the class file.", { y: 4.3 });
  s.addNotes(
    "Show the file in the IDE if you can, it is more convincing than a slide.\n\n" +
    "Insist: there is no behaviour anywhere in this file. Beginners assume @Component does something. It does not. It is a sticker.\n\n" +
    "Then set up the next slide: so if it does nothing, how does a class end up in the container? Let us look at what the compiler actually wrote."
  );
}

/* ---------- 11. Annotation diagram ---------- */
{
  diagramSlide("d2-annotation.png",
    "Walk the three boxes left to right, one sentence each: the sticker, the class file, the reader.\n\n" +
    "The metaphor to use out loud: the annotation is a sticker on a box, reflection is the pair of glasses, the container is the person who acts on what is written. Remove any of the three and nothing happens.\n\n" +
    "The red line at the bottom is the demo you are about to run."
  );
}

/* ---------- 12. javap demo ---------- */
{
  const s = light();
  title(s, "Proof: javap, and one word changed", "The demo that makes it click");
  code(s, [
    "$ javap -v AuditService.class",
    "",
    "RuntimeVisibleAnnotations:",
    "  0: #14()",
    "    io.minispring.container.annotation.Component",
  ], { x: 0.5, y: 1.5, w: 4.5, h: 1.7, size: 11 });
  s.addText("RUNTIME", {
    x: 0.5, y: 3.3, w: 4.5, h: 0.35, isTextBox: true, fontFace: BODY, fontSize: 14, bold: true, color: ACCENT, align: "center", margin: 0,
  });
  code(s, [
    "$ javap -v AuditService.class",
    "",
    "RuntimeInvisibleAnnotations:",
    "  0: #14()",
    "    io.minispring.container.annotation.Component",
  ], { x: 5.1, y: 1.5, w: 4.5, h: 1.7, size: 11 });
  s.addText("CLASS — the default", {
    x: 5.1, y: 3.3, w: 4.5, h: 0.35, isTextBox: true, fontFace: BODY, fontSize: 14, bold: true, color: WARN, align: "center", margin: 0,
  });
  callout(s, "One word, and the scanner finds nothing — with no error message. Everyone hits this once.", { y: 4.2, color: WARN });
  s.addNotes(
    "DEMO. Run javap live if the room is small, otherwise play the recording. Rehearse it: the command must be one line, already typed, in a large font.\n\n" +
    "Script: compile, run javap, point at RuntimeVisibleAnnotations. Then change RUNTIME to CLASS in Component.java, recompile, run javap again: the attribute name changed. Run the app: NoSuchBeanException.\n\n" +
    "Bolt for seniors: SOURCE retention disappears at compile time entirely — that is what Lombok and annotation processors use, because they work before the class file exists."
  );
}

/* ---------- 13. Retention / Target ---------- */
{
  const s = light();
  title(s, "The three retentions, and who uses them", null);
  table(s, [
    header(["Retention", "Lives until", "Read by", "Example"]),
    [mono("SOURCE"), "the compiler", "annotation processors", mono("@Override, Lombok")],
    [mono("CLASS"), "the class file", "bytecode tools", mono("nullability annotations")],
    [{ text: "RUNTIME", options: { fontFace: MONO, fontSize: 11, bold: true, color: ACCENT } }, "for ever", "reflection, at run time", mono("@Component, @Autowired")],
  ], { y: 1.7, colW: [1.7, 2.1, 2.6, 2.6], size: 12 });
  s.addText("@Target says where the sticker may be put: a class, a method, a parameter. The compiler enforces it, nothing else does.", {
    x: 0.5, y: 3.5, w: 9, h: 0.6, isTextBox: true, fontFace: BODY, fontSize: 15, color: MUTED, margin: 0,
  });
  callout(s, "A framework that reads annotations at run time needs RUNTIME. That is the whole rule.", { y: 4.3 });
  s.addNotes(
    "Keep this slide to sixty seconds, it is a reference slide people photograph.\n\n" +
    "The useful nuance: CLASS is the default, which is exactly the wrong default for framework authors. That is why the mistake is so common.\n\n" +
    "Bolt: Spring 6 and Quarkus read annotations at build time now, which brings us back to the SOURCE and CLASS columns. The annotations did not change; the moment they are read did."
  );
}

/* ---------- 14. History ---------- */
{
  const s = light();
  title(s, "A short history of the three ingredients", "Spring could not have existed before 2004");
  const rows = [
    ["1997", "Java 1.1", "Reflection", "read a class at run time"],
    ["2000", "Java 1.3", "Dynamic proxies", "your @Transactional"],
    ["2004", "Java 5", "Annotations (JSR 175)", "the sticker"],
    ["2006", "Java 6", "@PostConstruct (JSR 250)", "the lifecycle"],
    ["2020+", "Spring 6, GraalVM", "AOT processing", "read them at build time"],
  ];
  table(s, [header(["Year", "Where", "What arrived", "Why it matters here"])].concat(rows.map((r) => [mono(r[0]), r[1], { text: r[2], options: { bold: true } }, r[3]])),
    { y: 1.65, colW: [1.1, 1.9, 2.9, 3.1], size: 12 });
  callout(s, "Spring did not invent any of this. It assembled three JDK features nobody was using together.", { y: 4.4 });
  s.addNotes(
    "This slide buys you credibility with the senior half of the room, and it gives the junior half a story to hang the concepts on.\n\n" +
    "The punchline: every ingredient of Spring existed in the JDK. What Spring added was the assembly and the opinion.\n\n" +
    "The last row is the forward-looking one: we are now moving the reading from run time to build time, which is why native images are possible. Do not go deeper unless asked."
  );
}

/* ---------- 15. Reflection API used ---------- */
{
  const s = light();
  title(s, "The reflection API, as used by the container", "Six calls. That is the whole toolbox.");
  table(s, [
    header(["The call", "What it does", "Where in minispring"]),
    [mono("Class.forName(n, false, cl)"), "load a class without initialising it", mono("ClasspathScanner")],
    [mono("type.isAnnotationPresent(..)"), "read the sticker", mono("ClasspathScanner")],
    [mono("type.getDeclaredConstructors()"), "find the constructor to use", mono("BeanDefinitionReader")],
    [mono("constructor.newInstance(args)"), "create the bean", mono("BeanFactory")],
    [mono("method.invoke(bean)"), "run @PostConstruct", mono("BeanFactory")],
    [mono("Proxy.newProxyInstance(..)"), "wrap the bean", mono("TransactionalProcessor")],
  ], { y: 1.6, colW: [3.5, 3.1, 2.4], size: 11 });
  callout(s, "If you know these six calls, you can write a dependency injection container.", { y: 4.5 });
  s.addNotes(
    "This is the slide that turns 'framework magic' into 'a list of JDK methods'. Read the first column slowly.\n\n" +
    "Mention setAccessible in passing: the container calls constructors and methods that are not public, and that is normal — it is a deliberate, documented choice in the code, with a Sonar suppression that explains why.\n\n" +
    "Bolt: this is also why reflection-heavy frameworks are slow to start and hard to compile natively. Each of these calls costs, and none of them is visible to the compiler."
  );
}

/* ---------- Section 4 ---------- */
section(4, "Every annotation, and who reads it", "minispring, Spring, and the XML we used to write",
  "Transition, and the core of the talk. Fifteen minutes. The table on the next slide is the one people will photograph — pause on it and say so, they will get their phones out.\n\nThen we zoom into three annotations only. Resist the urge to cover all of them.");

/* ---------- 16. Mapping table 1 ---------- */
{
  const s = light();
  title(s, "The mapping table (1/2)", "Same model, three ways to feed it");
  table(s, [
    header(["Annotation", "Read by, in minispring", "The Spring class", "In XML, before"]),
    [mono9("@Component"), mono9("ClasspathScanner"), mono9("ClassPathBeanDefinitionScanner"), mono9("<context:component-scan>")],
    [mono9("@Autowired"), mono9("BeanDefinitionReader"), mono9("AutowiredAnnotation...Processor"), mono9("<constructor-arg ref>")],
    [mono9("@Qualifier @Primary"), mono9("DependencyResolver"), mono9("QualifierAnnotation...Resolver"), mono9("<qualifier>, primary=")],
    [mono9("@Scope"), mono9("BeanDefinitionReader"), mono9("ScopeMetadataResolver"), mono9("scope=\"prototype\"")],
    [mono9("@PostConstruct"), mono9("LifecycleMethods"), mono9("CommonAnnotation...Processor"), mono9("init-method=")],
  ], { y: 1.6, colW: [1.9, 2.2, 2.9, 2.0], size: 9 });
  callout(s, "The annotation is never the mechanism. It is one of three ways to write the same BeanDefinition.", { y: 4.45 });
  s.addNotes(
    "Say the promise out loud: photograph this slide, it is the map of the whole framework.\n\n" +
    "Then use the last column to make the point that annotations are not special. Everyone who wrote Spring XML in 2010 recognises init-method and scope=prototype. The container has not changed; the way we write metadata has.\n\n" +
    "Do not read every row. Read the @Component row and the @PostConstruct row, then move on."
  );
}

/* ---------- 17. Mapping table 2 ---------- */
{
  const s = light();
  title(s, "The mapping table (2/2)", "Where Boot goes beyond what XML could express");
  table(s, [
    header(["Annotation", "Read by, in minispring", "The Spring class", "In XML, before"]),
    [mono9("@Transactional"), mono9("TransactionalProcessor"), mono9("TransactionInterceptor"), mono9("<tx:advice> + <aop:config>")],
    [mono9("@Configuration @Bean"), mono9("BeanDefinitionReader"), mono9("ConfigurationClassPostProcessor"), mono9("factory-bean, factory-method")],
    [mono9("@RestController"), mono9("DispatcherServer"), mono9("RequestMappingHandlerMapping"), mono9("<mvc:annotation-driven>")],
    [mono9("ApplicationListener"), mono9("ApplicationEventMulticaster"), mono9("SimpleApplicationEvent...caster"), mono9("a <bean> implementing it")],
    [{ text: "@Conditional", options: { fontFace: MONO, fontSize: 9, bold: true, color: ACCENT } }, mono9("ConditionEvaluator"), mono9("ConditionEvaluator (same name)"), { text: "no equivalent", options: { color: WARN, bold: true, fontSize: 9 } }],
  ], { y: 1.6, colW: [1.9, 2.2, 2.9, 2.0], size: 9 });
  callout(s, "The last row is why Spring Boot could not have been built in the XML era.", { y: 4.45, color: WARN });
  s.addNotes(
    "The punchline is the last row, and it is the bridge to part 6. XML could describe beans; it could not describe a decision that depends on the classpath and on what you already declared.\n\n" +
    "If someone objects that XML had profiles: true, and profiles are the closest ancestor of @Conditional. Say it, it shows you know the history.\n\n" +
    "Note for yourself: several class names are identical on both sides because I deliberately mirrored Spring's naming. Point it out, it helps people navigate the real Spring source afterwards."
  );
}

/* ---------- 18. Zoom @Component ---------- */
{
  const s = light();
  title(s, "Zoom 1 — @Component: finding classes", "Java has no API to list the classes of a package");
  code(s, [
    "private static boolean isInstantiableComponent(Class<?> type) {",
    "    int modifiers = type.getModifiers();",
    "    return isComponent(type)",
    "            && !type.isInterface()",
    "            && !Modifier.isAbstract(modifiers)",
    "            && (!type.isMemberClass() || Modifier.isStatic(modifiers));",
    "}",
  ], { x: 0.5, y: 1.5, w: 9, h: 1.85, size: 12 });
  bullets(s, [
    "Ask the ClassLoader for the package directory, then walk it with Files.walk",
    "Load with Class.forName(name, false, loader): finding must not run static initialisers",
    "Sort the result, so that startup order never depends on the file system",
  ], { y: 3.45, h: 1.1, size: 14 });
  s.addNotes(
    "The line that surprises people: there is no way to ask Java for the classes of a package. You ask the class loader where the folder is and you read files. Spring does the same, with more cases.\n\n" +
    "Bolt: this is also why my scanner does not see classes inside jars, and why Spring Boot's auto-configuration uses an index file instead of scanning. That link pays off in part 6.\n\n" +
    "Mention the sorting: it is a one-line decision that makes startup deterministic and tests reproducible."
  );
}

/* ---------- 19. Zoom @Autowired ---------- */
{
  const s = light();
  title(s, "Zoom 2 — choosing a constructor, then a bean", "Two rules, and an error message that does the teaching");
  bullets(s, [
    "One constructor: use it, no annotation needed — that is why @Autowired disappeared from modern Spring code",
    "Several: use the one marked @Autowired; several marked, or none: fail, and list the candidates",
    "Then, per parameter: type match, then @Qualifier by name, then @Primary. Never a guess.",
  ], { y: 1.5, h: 1.4, size: 15 });
  code(s, [
    "2 beans of type com.example.PaymentGateway match, required by",
    "parameter 'gateway' of bean 'checkout': [stripeGateway, paypalGateway].",
    "Mark one with @Primary, or choose one with @Qualifier(\"name\").",
  ], { x: 0.5, y: 3.0, w: 9, h: 1.15, size: 11 });
  callout(s, "An error message that names the parameter and every candidate is worth more than a page of documentation.", { y: 4.35 });
  s.addNotes(
    "The first bullet answers a question juniors always have: why do I see @Autowired in old code but not in new code? Because with a single constructor it became unnecessary in Spring 4.3.\n\n" +
    "Read the error message out loud, slowly. Then say: this is a design decision, not a detail. Spring's version of this message is famously unhelpful, and it costs the ecosystem thousands of hours.\n\n" +
    "Bolt: parameter names only survive compilation with the -parameters flag. Without it you get arg0, which is why the message would be useless."
  );
}

/* ---------- 20. Zoom @Transactional ---------- */
{
  diagramSlide("d4-proxy.png",
    "Build the picture in words before the audience reads it: the caller does not hold your bean, it holds a proxy that implements the same interface.\n\n" +
    "The post-processor is 40 lines: find methods annotated @Transactional, ask the JDK for a proxy, call the transaction manager around each call.\n\n" +
    "Then pause, because the next slide is the one people will remember."
  );
}

/* ---------- 21. Self-invocation ---------- */
{
  const s = light();
  title(s, "The bug you have already hit", "The most common @Transactional mistake in production");
  code(s, [
    "@Override",
    "public void placeAll(List<String> items) {",
    "    items.forEach(this::placeOrder);   // \"this\" is the bean, not the proxy",
    "}                                      // -> no transaction, and no warning",
  ], { x: 0.5, y: 1.5, w: 9, h: 1.4, size: 13 });
  s.addText("The test that pins it down:", {
    x: 0.5, y: 3.05, w: 9, h: 0.3, isTextBox: true, fontFace: BODY, fontSize: 14, color: MUTED, margin: 0,
  });
  code(s, [
    "void selfInvocationBypassesTheProxyAndStartsNoTransaction() { ... }",
  ], { x: 0.5, y: 3.4, w: 9, h: 0.55, size: 12 });
  callout(s, "Same behaviour in Spring, with JDK proxies and with CGLIB. Fifteen lines of your own code explain a day of debugging.", { y: 4.2, color: WARN });
  s.addNotes(
    "Ask the room first: who has already lost time on a @Transactional that did nothing? Hands go up, and you have their full attention.\n\n" +
    "Then explain with the diagram still fresh: the proxy wraps the bean from outside. Inside the bean, this is the bean. An internal call never crosses the proxy boundary.\n\n" +
    "Say the workarounds, briefly: split into two beans, or inject the bean into itself, or use AopContext. The first one is the honest fix.\n\n" +
    "This is the story that got the most reactions on my LinkedIn post. It is relatable, not academic."
  );
}

/* ---------- 22. Lifecycle recap ---------- */
{
  diagramSlide("d1-lifecycle.png",
    "Recap slide. Walk it once, quickly, naming the annotation that matters at each step.\n\n" +
    "The insight worth stating: post-processing comes after @PostConstruct, so a proxy always wraps a fully initialised object. And @PreDestroy runs on the raw bean, because a JDK proxy only exposes interface methods.\n\n" +
    "If you are running late, this is the slide to skip — it repeats what they just saw."
  );
}

/* ---------- Section 5 ---------- */
section(5, "Invent your own annotation", "The moment this stops being someone else's framework",
  "Transition. Eight minutes. This is the part that converts spectators into people who will try it tonight.\n\nIf the room is with you, do it live in the IDE. If not, the three slides are enough.");

/* ---------- 23. @Timed step by step ---------- */
{
  const s = light();
  title(s, "@Timed, in three steps", "The same pattern as @Transactional");
  code(s, [
    "@Retention(RUNTIME) @Target(METHOD)",
    "public @interface Timed { }",
  ], { x: 0.5, y: 1.45, w: 9, h: 0.75, size: 12 });
  s.addText("1. The sticker", { x: 0.5, y: 2.25, w: 4, h: 0.28, isTextBox: true, fontFace: BODY, fontSize: 12, bold: true, color: ACCENT, margin: 0 });
  code(s, [
    "public Object postProcessAfterInitialization(Object bean, String name) {",
    "    // if a method carries @Timed, return a proxy that measures the call",
    "    return Proxy.newProxyInstance(loader, interfaces, handler);",
    "}",
  ], { x: 0.5, y: 2.6, w: 9, h: 1.2, size: 12 });
  s.addText("2. A BeanPostProcessor that wraps the bean   ·   3. A test that proves the duration is logged", {
    x: 0.5, y: 3.85, w: 9, h: 0.3, isTextBox: true, fontFace: BODY, fontSize: 12, bold: true, color: ACCENT, margin: 0,
  });
  callout(s, "Thirty lines. You have just written the mechanism behind @Cacheable, @Retry and @Async.", { y: 4.3 });
  s.addNotes(
    "DEMO if possible. Write the annotation, copy TransactionalProcessor, rename, run the test. Five minutes, rehearsed.\n\n" +
    "The sentence to land: every method-level annotation in Spring works this way. @Cacheable, @Retry, @Async, @PreAuthorize — same pattern, more edge cases.\n\n" +
    "Then warn them, honestly: in real projects, use the existing ones. The point is not to rewrite Spring, it is to stop being afraid of it."
  );
}

/* ---------- Section 6 ---------- */
section(6, "The Boot layer: auto-configuration", "How @Conditional decides what gets wired",
  "Transition. Six minutes. Tell the true story: this part exists because someone asked the question in a LinkedIn comment. It makes the talk feel alive, and it invites questions.");

/* ---------- 24. Autoconfig diagram ---------- */
{
  diagramSlide("d5-autoconfig.png",
    "Walk the four boxes. Insist on the first one: your beans are registered first. Everything else depends on that ordering.\n\n" +
    "Say what auto-configuration is in one sentence: a plugin that contributes bean definitions through an extension point, after you, and only for what is missing.\n\n" +
    "In my implementation the container knows nothing about it: it is a separate module, 300 lines."
  );
}

/* ---------- 25. Two rules ---------- */
{
  const s = light();
  title(s, "Two rules, and everything follows", null);
  s.addShape(pres.ShapeType.roundRect, { x: 0.5, y: 1.5, w: 4.35, h: 1.5, fill: { color: CODE_BG }, line: { color: "DEE2E6", width: 1 }, rectRadius: 0.08 });
  s.addText("1. Conditions read definitions,\nnever instances", { x: 0.7, y: 1.65, w: 4, h: 0.7, isTextBox: true, fontFace: BODY, fontSize: 16, bold: true, color: TEXT, margin: 0 });
  s.addText("Deciding whether a bean should exist must not create beans, or the question answers itself.", { x: 0.7, y: 2.35, w: 4, h: 0.6, isTextBox: true, fontFace: BODY, fontSize: 13, color: MUTED, margin: 0 });
  s.addShape(pres.ShapeType.roundRect, { x: 5.15, y: 1.5, w: 4.35, h: 1.5, fill: { color: CODE_BG }, line: { color: "DEE2E6", width: 1 }, rectRadius: 0.08 });
  s.addText("2. Auto-configurations\nregister last", { x: 5.35, y: 1.65, w: 4, h: 0.7, isTextBox: true, fontFace: BODY, fontSize: 16, bold: true, color: TEXT, margin: 0 });
  s.addText("Only then can @ConditionalOnMissingBean mean \"unless you declared your own\".", { x: 5.35, y: 2.35, w: 4, h: 0.6, isTextBox: true, fontFace: BODY, fontSize: 13, color: MUTED, margin: 0 });
  code(s, [
    "@Bean",
    "@ConditionalOnMissingBean",
    "public Notifier notifier() { return message -> System.out.println(\"NOTIFY   \" + message); }",
  ], { x: 0.5, y: 3.2, w: 9, h: 1.05, size: 11 });
  callout(s, "Same classes, opposite registration order, different application. There is a test for it.", { y: 4.45, color: WARN });
  s.addNotes(
    "These two rules are the answer to Karim's question, and the takeaway of the whole section.\n\n" +
    "Rule two explains the warning in Spring's own documentation: @ConditionalOnMissingBean is only reliable inside an auto-configuration. Now they know why, instead of just obeying.\n\n" +
    "Bolt: @ConditionalOnClass has to name classes as strings in my version, because reading a Class attribute that points at an absent class fails — which is exactly the case it exists for. Spring solves it by reading annotations from the bytecode with ASM. One constraint, and a dependency people never question suddenly makes sense."
  );
}

/* ---------- 26. Report ---------- */
{
  const s = light();
  title(s, "\"Why is my bean not there?\"", "Every decision is recorded, with its reason");
  code(s, [
    "CONDITION EVALUATION REPORT",
    "",
    "Positive matches:",
    "  none",
    "",
    "Negative matches:",
    "  NotifierAutoConfiguration",
    "    - property 'minispring.notifier.enabled' is 'false', expected 'true'",
  ], { x: 0.5, y: 1.5, w: 9, h: 2.5, size: 12 });
  callout(s, "In Spring Boot: start with --debug. In minispring: minispring.autoconfigure.report=true.", { y: 4.2 });
  s.addNotes(
    "This is the practical takeaway people can use on Monday morning, whatever framework they run.\n\n" +
    "Say it plainly: most developers debug auto-configuration by guessing. The report turns guessing into reading.\n\n" +
    "Demo option: run the three-scenario demo from the repository. Default bean, your own bean, starter switched off. Ninety seconds."
  );
}

/* ---------- Section 7 ---------- */
section(7, "What it costs", "The honest slide",
  "Transition. Three minutes. Never end a rebuild talk on a victory lap — the audience knows Spring does infinitely more. Owning that is what makes the rest credible.");

/* ---------- 27. Cost ---------- */
{
  const s = light();
  title(s, "100 ms against 1.7 s, and why that is fair", "The same application, on both containers");
  table(s, [
    header(["Spring Boot does this", "minispring does not"]),
    ["Evaluates hundreds of conditional auto-configurations", "reads one small index file"],
    ["Reads annotation metadata from the whole classpath with ASM", "reflects over one package"],
    ["Builds an Environment from many property sources", "four sources, in order"],
    ["Initialises a logging system, generates CGLIB subclasses", "neither"],
    ["Runs a real servlet container", "a 100-line HTTP dispatcher"],
  ], { y: 1.65, colW: [5.0, 4.0], size: 12 });
  callout(s, "The gap is not a flaw in Spring. It is the price of the features I chose not to build.", { y: 4.4 });
  s.addNotes(
    "Deliver this without irony. The audience includes people who maintain large Spring applications; respect that.\n\n" +
    "If someone asks how to make Spring Boot start faster: lazy initialisation, fewer starters, and AOT with native images. Say it in one sentence, do not start a second talk.\n\n" +
    "Also own my own limitations here: constructor injection only, JDK proxies only, no jar scanning, not thread-safe during startup. They are choices, and they are in the README."
  );
}

/* ---------- 28. Takeaways ---------- */
{
  const s = pres.addSlide();
  s.background = { color: INK };
  s.addText("Three things to take home", {
    x: 0.7, y: 0.6, w: 8.6, h: 0.6, isTextBox: true, fontFace: HEAD, fontSize: 30, bold: true, color: "FFFFFF", margin: 0,
  });
  const takeaways = [
    ["An annotation does nothing", "Something has to read it. That something is now readable code, not magic."],
    ["The container is a Map and a few rules", "Definitions first, instances second. Every feature follows that order."],
    ["You can write your own", "Thirty lines gives you @Timed. The rest is edge cases."],
  ];
  takeaways.forEach((t, i) => {
    const y = 1.5 + i * 1.05;
    s.addShape(pres.ShapeType.ellipse, { x: 0.7, y, w: 0.5, h: 0.5, fill: { color: ACCENT } });
    s.addText(String(i + 1), { x: 0.7, y, w: 0.5, h: 0.5, isTextBox: true, fontFace: BODY, fontSize: 16, bold: true, color: "FFFFFF", align: "center", valign: "middle", margin: 0 });
    s.addText(t[0], { x: 1.4, y: y - 0.05, w: 8, h: 0.35, isTextBox: true, fontFace: BODY, fontSize: 18, bold: true, color: "FFFFFF", margin: 0 });
    s.addText(t[1], { x: 1.4, y: y + 0.3, w: 8, h: 0.4, isTextBox: true, fontFace: BODY, fontSize: 13, color: "9FB3C8", margin: 0 });
  });
  s.addShape(pres.ShapeType.roundRect, { x: 0.7, y: 4.65, w: 5.2, h: 0.55, fill: { color: ACCENT }, rectRadius: 0.1 });
  s.addText("github.com/ELMAALMIA/minispring", {
    x: 0.7, y: 4.65, w: 5.2, h: 0.55, isTextBox: true, fontFace: MONO, fontSize: 13, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
  });
  s.addText("Read it commit by commit: one concept each, all green.", {
    x: 6.1, y: 4.72, w: 3.4, h: 0.45, isTextBox: true, fontFace: BODY, fontSize: 12, color: "9FB3C8", margin: 0,
  });
  s.addNotes(
    "Close on the three sentences, then stop talking. Do not add a fourth point.\n\n" +
    "The call to action that works: clone it and read the commit history in order, each commit adds one concept with its tests.\n\n" +
    "Then open the floor. Expected questions: why no CGLIB; how do cycles work; is it production-ready (no, and say so); how long did it take."
  );
}

pres.writeFile({ fileName: OUT }).then(() => console.log("written: " + OUT));
