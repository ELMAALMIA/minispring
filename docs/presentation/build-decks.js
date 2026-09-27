/**
 * Builds the two talks:
 *   minispring-talk-1-beans.pptx    — how a bean is born
 *   minispring-talk-2-proxies.pptx  — what happens to it afterwards
 *
 * Run with: node docs/presentation/build-decks.js   (needs: npm install pptxgenjs)
 */
const pptxgen = require("pptxgenjs");
const path = require("path");

const DIAGRAMS = path.join(__dirname, "diagrams");
const OUT_DIR = __dirname.replace(/\\/g, "/");

const INK = "1B2430";
const INK_SOFT = "2E3A47";
const PAPER = "FFFFFF";
const TEXT = "22303C";
const MUTED = "5B6B7A";
const ACCENT = "2F9E44";
const WARN = "E8590C";
const CODE_BG = "F1F3F5";
const BODY = "Calibri";
const HEAD = "Cambria";
const MONO = "Courier New";

/** The deck currently being built. Every helper writes into it. */
let pres;

function newDeck(deckTitle) {
  pres = new pptxgen();
  pres.layout = "LAYOUT_16x9"; // 10 x 5.625 in
  pres.author = "Ayoub EL Maalmi";
  pres.title = deckTitle;
}

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
  s.addShape(pres.ShapeType.ellipse, { x: 0.9, y: 2.1, w: 1.1, h: 1.1, fill: { color: ACCENT } });
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

/** Marks a slide you can drop live when you are running late. */
const CUT = "[CUT IF LATE] ";

function deckTitleSlide(part, heading, promise, minutes) {
  const s = pres.addSlide();
  s.background = { color: INK };
  s.addText("Spring is not magic  ·  " + part, {
    x: 0.8, y: 1.35, w: 8.5, h: 0.4, isTextBox: true,
    fontFace: BODY, fontSize: 16, color: ACCENT, margin: 0,
  });
  s.addText(heading, {
    x: 0.8, y: 1.8, w: 8.5, h: 0.95, isTextBox: true,
    fontFace: HEAD, fontSize: 40, bold: true, color: "FFFFFF", margin: 0,
  });
  s.addText(promise, {
    x: 0.8, y: 2.85, w: 8.5, h: 0.5, isTextBox: true,
    fontFace: BODY, fontSize: 18, color: "9FB3C8", margin: 0,
  });
  s.addShape(pres.ShapeType.roundRect, { x: 0.8, y: 3.7, w: 4.6, h: 0.55, fill: { color: ACCENT }, rectRadius: 0.1 });
  s.addText("github.com/ELMAALMIA/minispring", {
    x: 0.8, y: 3.7, w: 4.6, h: 0.55, isTextBox: true,
    fontFace: MONO, fontSize: 13, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
  });
  s.addText("Ayoub EL Maalmi  ·  Java & Spring Boot  ·  " + minutes + " minutes", {
    x: 0.8, y: 4.5, w: 8.5, h: 0.4, isTextBox: true,
    fontFace: BODY, fontSize: 14, color: "7C93A8", margin: 0,
  });
  return s;
}

function agenda(items, sub, notes) {
  const s = light();
  title(s, "Where we are going", sub);
  items.forEach((it, i) => {
    const x = i < 3 ? 0.55 : 5.15;
    const y = 1.75 + (i % 3) * 1.05;
    s.addShape(pres.ShapeType.ellipse, { x, y, w: 0.42, h: 0.42, fill: { color: i < 3 ? ACCENT : INK_SOFT } });
    s.addText(String(i + 1), {
      x, y, w: 0.42, h: 0.42, isTextBox: true,
      fontFace: BODY, fontSize: 14, bold: true, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
    });
    s.addText(it[0], {
      x: x + 0.58, y: y - 0.04, w: 3.7, h: 0.3, isTextBox: true,
      fontFace: BODY, fontSize: 15, bold: true, color: TEXT, margin: 0,
    });
    s.addText(it[1], {
      x: x + 0.58, y: y + 0.26, w: 3.7, h: 0.5, isTextBox: true,
      fontFace: BODY, fontSize: 12, color: MUTED, margin: 0,
    });
  });
  s.addNotes(notes);
  return s;
}

function takeawaysSlide(heading, items, footerLeft, footerRight, notes) {
  const s = pres.addSlide();
  s.background = { color: INK };
  s.addText(heading, {
    x: 0.7, y: 0.6, w: 8.6, h: 0.6, isTextBox: true,
    fontFace: HEAD, fontSize: 30, bold: true, color: "FFFFFF", margin: 0,
  });
  items.forEach((t, i) => {
    const y = 1.5 + i * 1.05;
    s.addShape(pres.ShapeType.ellipse, { x: 0.7, y, w: 0.5, h: 0.5, fill: { color: ACCENT } });
    s.addText(String(i + 1), {
      x: 0.7, y, w: 0.5, h: 0.5, isTextBox: true,
      fontFace: BODY, fontSize: 16, bold: true, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
    });
    s.addText(t[0], { x: 1.4, y: y - 0.05, w: 8, h: 0.35, isTextBox: true, fontFace: BODY, fontSize: 18, bold: true, color: "FFFFFF", margin: 0 });
    s.addText(t[1], { x: 1.4, y: y + 0.3, w: 8, h: 0.45, isTextBox: true, fontFace: BODY, fontSize: 13, color: "9FB3C8", margin: 0 });
  });
  s.addShape(pres.ShapeType.roundRect, { x: 0.7, y: 4.65, w: 5.2, h: 0.55, fill: { color: ACCENT }, rectRadius: 0.1 });
  s.addText(footerLeft, {
    x: 0.7, y: 4.65, w: 5.2, h: 0.55, isTextBox: true,
    fontFace: MONO, fontSize: 13, color: "FFFFFF", align: "center", valign: "middle", margin: 0,
  });
  s.addText(footerRight, {
    x: 6.1, y: 4.68, w: 3.4, h: 0.5, isTextBox: true,
    fontFace: BODY, fontSize: 12, color: "9FB3C8", margin: 0,
  });
  s.addNotes(notes);
  return s;
}

/* ================= shared slides ================= */

function hook() {
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

function timeline() {
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
    "The key line is the callout, and it pays off twice: on the XML slide later, and in part 2 with auto-configuration.\n\n" +
    "If someone asks why XML lost: annotations put the metadata next to the code it describes. XML kept it in a file nobody opened. Both feed the same model.\n\n" +
    "Do not spend more than ninety seconds here."
  );
}

/* ================= deck 1 ================= */

function wiringByHand() {
  const s = light();
  title(s, "Before the container: you wire it yourself", "Everybody has written this");
  code(s, [
    "var repository = new InMemoryOrderRepository();",
    "var audit      = new AuditService();",
    "var service    = new OrderServiceImpl(repository, audit, publisher);",
    "var controller = new OrderController(service);",
  ], { x: 0.5, y: 1.5, w: 9, h: 1.5, size: 13 });
  bullets(s, [
    "It works — until the graph has forty objects instead of four",
    "Every caller has to know the whole graph, just to build one object",
    "Swapping an implementation means editing every place that says new",
    "And someone has to decide who gets closed, in which order",
  ], { y: 3.15, h: 1.3, size: 15 });
  callout(s, "The problem was never typing new. It is that the caller has to know everything.", { y: 4.55, color: WARN });
  s.addNotes(
    "Start here, not at the container. Ask the room to picture this code in a real service with forty beans.\n\n" +
    "Take the four bullets one by one, they are the four pains that justify everything that follows. The last one matters more than people expect: shutdown order is exactly what @PreDestroy solves later.\n\n" +
    "Do not say 'boilerplate'. Say 'the caller has to know everything'. That is the real problem, and it sets up the next slide."
  );
}

function whatYouGain() {
  const s = light();
  title(s, "What you actually gain", "It is not fewer lines. It is who chooses.");
  code(s, [
    "@Component",
    "class OrderServiceImpl implements OrderService {",
    "    OrderServiceImpl(OrderRepository repository, AuditService audit) { ... }",
    "}",
    "",
    "try (var context = AnnotationApplicationContext.scan(\"com.example\")) {",
    "    context.getBean(OrderController.class);   // the graph is built for you",
    "}",
  ], { x: 0.5, y: 1.5, w: 9, h: 2.4, size: 13 });
  bullets(s, [
    "The class still asks for what it needs: nothing is hidden, it is all in the constructor",
    "But it no longer chooses which implementation it gets — that is the inversion of control",
    "So a test builds it with a fake repository, in one line, with no framework at all",
  ], { y: 3.95, h: 1.0, size: 14 });
  callout(s, "Dependency injection is a design decision. The container is only the tool that carries it out.", { y: 4.95 });
  s.addNotes(
    "The sentence to land, slowly: the class still asks for what it needs, it simply no longer decides what it gets.\n\n" +
    "The third bullet is the argument that convinces seniors: constructor injection means your tests need no framework. new OrderServiceImpl(fakeRepository, audit) and you are done. That is why field injection is discouraged — it takes that away.\n\n" +
    "Close with the callout: the pattern is the point, the container is the plumbing."
  );
}

function containerIsMap() {
  const s = light();
  title(s, "A container is a Map, plus rules", null);
  s.addShape(pres.ShapeType.roundRect, { x: 0.5, y: 1.45, w: 4.3, h: 1.0, fill: { color: INK }, rectRadius: 0.08 });
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
    "Let something wrap a bean before it is handed out   (part 2)",
    "Decide whether a bean should exist at all   (part 2)",
  ], { x: 0.55, y: 2.6, w: 9, h: 1.9, size: 15 });
  s.addNotes(
    "Six rules. The first four are this talk, the last two are part 2. Say that: it tells the audience where the story stops today.\n\n" +
    "The line that lands: Spring's DefaultListableBeanFactory is 2,000 lines, but the model underneath is a map from name to object. Everything else is rules about how entries get in there, and in which order."
  );
}

function componentSource() {
  const s = light();
  title(s, "An annotation is an interface with no code", "The real file from the project");
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
    "Then set up the next slide: so if it does nothing, how does a class end up in the container?"
  );
}

function javapDemo() {
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

function retentionTable() {
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
    CUT + "this is a reference slide; the javap demo already made the point.\n\n" +
    "Keep it to sixty seconds. The useful nuance: CLASS is the default, which is exactly the wrong default for framework authors. That is why the mistake is so common.\n\n" +
    "Bolt: Spring 6 and Quarkus read annotations at build time now, which brings us back to the SOURCE and CLASS rows. The annotations did not change; the moment they are read did."
  );
}

function reflectionApi() {
  const s = light();
  title(s, "The reflection API, as used by the container", "Six calls. That is the whole toolbox.");
  table(s, [
    header(["The call", "What it does", "Where in minispring"]),
    [mono("Class.forName(n, false, cl)"), "load a class without initialising it", mono("ClasspathScanner")],
    [mono("type.isAnnotationPresent(..)"), "read the sticker", mono("ClasspathScanner")],
    [mono("type.getDeclaredConstructors()"), "find the constructor to use", mono("BeanDefinitionReader")],
    [mono("constructor.newInstance(args)"), "create the bean", mono("BeanFactory")],
    [mono("method.invoke(bean)"), "run @PostConstruct", mono("BeanFactory")],
    [mono("Proxy.newProxyInstance(..)"), "wrap the bean  (part 2)", mono("TransactionalProcessor")],
  ], { y: 1.6, colW: [3.5, 3.1, 2.4], size: 11 });
  callout(s, "If you know these six calls, you can write a dependency injection container.", { y: 4.5 });
  s.addNotes(
    "This is the slide that turns 'framework magic' into 'a list of JDK methods'. Read the first column slowly.\n\n" +
    "Mention setAccessible in passing: the container calls constructors and methods that are not public, and that is normal — it is a deliberate, documented choice in the code.\n\n" +
    "Bolt: this is also why reflection-heavy frameworks are slow to start and hard to compile natively. Each of these calls costs, and none of them is visible to the compiler."
  );
}

function mappingTableCreation() {
  const s = light();
  title(s, "Who reads what", "Same model, three ways to feed it");
  table(s, [
    header(["Annotation", "Read by, in minispring", "The Spring class", "In XML, before"]),
    [mono9("@Component"), mono9("ClasspathScanner"), mono9("ClassPathBeanDefinitionScanner"), mono9("<context:component-scan>")],
    [mono9("@Autowired"), mono9("BeanDefinitionReader"), mono9("AutowiredAnnotation...Processor"), mono9("<constructor-arg ref>")],
    [mono9("@Qualifier @Primary"), mono9("DependencyResolver"), mono9("QualifierAnnotation...Resolver"), mono9("<qualifier>, primary=")],
    [mono9("@Scope"), mono9("BeanDefinitionReader"), mono9("ScopeMetadataResolver"), mono9("scope=\"prototype\"")],
    [mono9("@PostConstruct @PreDestroy"), mono9("LifecycleMethods"), mono9("CommonAnnotation...Processor"), mono9("init-method=, destroy-method=")],
    [mono9("@Configuration @Bean"), mono9("BeanDefinitionReader"), mono9("ConfigurationClassPostProcessor"), mono9("factory-bean, factory-method")],
  ], { y: 1.6, colW: [2.1, 2.1, 2.8, 2.0], size: 9 });
  callout(s, "The annotation is never the mechanism. It is one of three ways to write the same BeanDefinition.", { y: 4.6 });
  s.addNotes(
    "Say the promise out loud: photograph this slide, it is the map of the part of Spring we are covering today.\n\n" +
    "Then use the last column to make the point that annotations are not special. Everyone who wrote Spring XML in 2010 recognises init-method and scope=prototype. The container has not changed; the way we write metadata has.\n\n" +
    "Do not read every row. Read the @Component row and the @PostConstruct row, then move on.\n\n" +
    "Note for yourself: several class names are identical on both sides because I deliberately mirrored Spring's naming. It helps people navigate the real Spring source afterwards."
  );
}

function threeWays() {
  const s = light();
  title(s, "The same bean, written three ways", "Different syntax, identical BeanDefinition");
  s.addText("2008 — XML", {
    x: 0.5, y: 1.45, w: 4.4, h: 0.25, isTextBox: true, fontFace: BODY, fontSize: 13, bold: true, color: MUTED, margin: 0,
  });
  code(s, [
    "<bean id=\"orderService\"",
    "      class=\"shop.OrderServiceImpl\">",
    "  <constructor-arg ref=\"orderRepository\"/>",
    "</bean>",
    "",
    "<bean id=\"orderRepository\"",
    "      class=\"shop.InMemoryOrderRepository\"",
    "      init-method=\"open\"",
    "      destroy-method=\"close\"",
    "      scope=\"singleton\"/>",
  ], { x: 0.5, y: 1.7, w: 4.4, h: 2.9, size: 9 });
  s.addText("Today — annotations", {
    x: 5.1, y: 1.42, w: 4.4, h: 0.25, isTextBox: true, fontFace: BODY, fontSize: 13, bold: true, color: ACCENT, margin: 0,
  });
  code(s, [
    "@Component",
    "class OrderServiceImpl implements OrderService {",
    "    OrderServiceImpl(OrderRepository repo) { }",
    "}",
  ], { x: 5.1, y: 1.7, w: 4.4, h: 1.15, size: 9 });
  s.addText("Today — Java configuration", {
    x: 5.1, y: 3.0, w: 4.4, h: 0.25, isTextBox: true, fontFace: BODY, fontSize: 13, bold: true, color: ACCENT, margin: 0,
  });
  code(s, [
    "@Configuration",
    "class AppConfig {",
    "    @Bean",
    "    OrderService orderService(OrderRepository r) {",
    "        return new OrderServiceImpl(r);",
    "    }",
    "}",
  ], { x: 5.1, y: 3.28, w: 4.4, h: 1.32, size: 9 });
  callout(s, "The container reads all three into the same object. XML did not disappear because it was wrong, but because the metadata sat far from the code.", { y: 4.75 });
  s.addNotes(
    "Ask the room first: who has already written Spring in XML? The hands tell you how much history to give.\n\n" +
    "Point at the three XML attributes that survived as annotations: init-method became @PostConstruct, destroy-method became @PreDestroy, scope became @Scope. Same model, different spelling.\n\n" +
    "Then be fair to XML: it kept configuration outside the code, which is exactly what you still want for things that change per environment. What killed it was putting the wiring there too.\n\n" +
    "Bolt: Java configuration is the interesting middle ground — it is code, so it is typed and refactorable, and it stays outside the class, so a library class you do not own can still become a bean."
  );
}

function zoomScanner() {
  const s = light();
  title(s, "Zoom 1 — finding the classes", "Java has no API to list the classes of a package");
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
    CUT + "the idea survives without the code, if you are behind.\n\n" +
    "The line that surprises people: there is no way to ask Java for the classes of a package. You ask the class loader where the folder is and you read files. Spring does the same, with more cases.\n\n" +
    "Bolt: this is also why my scanner does not see classes inside jars, and why Spring Boot's auto-configuration uses an index file instead of scanning — which is a part 2 story.\n\n" +
    "Mention the sorting: a one-line decision that makes startup deterministic and tests reproducible."
  );
}

function zoomConstructor() {
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

function scopes() {
  const s = light();
  title(s, "Scopes: how many instances exist", "One shared, or one per request");
  code(s, [
    "@Component                              // one instance, created at startup",
    "class Cache { }",
    "",
    "@Component @Scope(ScopeType.PROTOTYPE)  // a new one on every lookup",
    "class ShoppingCart { }",
  ], { x: 0.5, y: 1.5, w: 9, h: 1.65, size: 12 });
  bullets(s, [
    "Singletons are created at startup, so a wiring mistake fails then — not at three in the morning",
    "A prototype is created on every request, and the container keeps no reference to it",
    "Which is why a prototype never receives @PreDestroy: nobody is holding it for you",
  ], { y: 3.25, h: 1.15, size: 14 });
  callout(s, "A prototype injected into a singleton is created once, and once only. Same in Spring, and it surprises everyone.", { y: 4.5, color: WARN });
  s.addNotes(
    "The callout is the part worth the slide. Explain why: the singleton is built once, so its constructor runs once, so it receives exactly one prototype and keeps it for ever.\n\n" +
    "The fix in Spring: inject an ObjectProvider or a Provider and ask for a fresh instance each time. In my container, ask the context. Say it in one sentence, do not go further.\n\n" +
    "Two tests in the repository state both rules: returnsANewPrototypeOnEveryLookup and injectsAPrototypeIntoASingletonOnlyOnce."
  );
}

function lifecycle() {
  const s = light();
  title(s, "The lifecycle: after injection, before the end", "Order is the whole feature");
  code(s, [
    "init service with repository true      <- @PostConstruct, after injection",
    "",
    "destroy controller                     <- close(), in reverse creation order",
    "destroy service",
    "destroy repository",
  ], { x: 0.5, y: 1.5, w: 9, h: 1.65, size: 12 });
  bullets(s, [
    "@PostConstruct runs after injection, so the dependencies are usable — that is its only purpose",
    "close() destroys in reverse creation order, so a bean dies before what it depends on",
    "A failing @PreDestroy is logged, and the other beans are still closed",
    "If startup fails halfway, what was already created is destroyed before the error propagates",
  ], { y: 3.25, h: 1.4, size: 14 });
  s.addNotes(
    "This is the slide that answers the fourth pain from the wiring-by-hand slide: who closes what, and in which order. Point back to it.\n\n" +
    "Reverse order is not a detail: a repository must still work while the service that uses it is shutting down. Spring does exactly the same.\n\n" +
    "Bolt: the container keeps the raw bean for @PreDestroy, not the proxy, because a JDK proxy only exposes interface methods. That is a part 2 detail, drop it if nobody is asking."
  );
}

function circularDependencies() {
  const s = light();
  title(s, "Circular dependencies: why a constructor cannot", "And why that is good news");
  code(s, [
    "class Chicken { Chicken(Egg egg) { } }",
    "class Egg     { Egg(Chicken chicken) { } }",
    "",
    "Circular dependency: rock -> paper -> scissors -> rock. Constructor injection cannot",
    "create a bean before its own dependencies exist; move the shared logic into a",
    "separate bean to break the cycle.",
  ], { x: 0.5, y: 1.5, w: 9, h: 1.95, size: 11 });
  bullets(s, [
    "With constructors it is simply impossible: A cannot exist before B if each needs the other",
    "Spring resolves cycles only for field injection, by handing out a half-built object from an early cache",
    "So a cycle is a design problem, and constructor injection is what makes it visible",
  ], { y: 3.55, h: 1.15, size: 14 });
  callout(s, "The container refuses, and shows the whole chain. That is a feature, not a limitation.", { y: 4.8 });
  s.addNotes(
    "A classic interview question, and now you can answer it from the mechanism rather than from memory.\n\n" +
    "How the chain is printed: the container keeps the beans being created in an insertion-ordered set. When one is requested while still in that set, the set read from that bean onwards is the cycle.\n\n" +
    "If someone says Spring handles cycles fine: it does, for field injection, and Spring Boot 2.6 turned that off by default. The reason is exactly this slide."
  );
}

/* ================= deck 2 ================= */

function recapRules() {
  const s = light();
  title(s, "Where we left off", "Part 1, in four sentences");
  bullets(s, [
    "An annotation does nothing. Something has to read it — and javap showed us exactly what it reads.",
    "A definition describes a bean; the factory creates it after its dependencies, recursively.",
    "Scopes decide how many exist; @PostConstruct and @PreDestroy decide when things happen.",
    "At that point the bean is built, initialised, and about to be handed to you.",
  ], { y: 1.6, h: 2.2, size: 16 });
  callout(s, "Today: what happens between 'the bean is built' and 'you get it' — and who decides it should exist at all.", { y: 4.1 });
  s.addNotes(
    "Three minutes maximum. The room is either the same one as last time, or a new one: this slide has to work in both cases.\n\n" +
    "If a few people saw part 1, ask them to nod. Then say the one thing they need for today: the container builds an object, and only then does it decide what to hand over.\n\n" +
    "The diagram before this slide carries the detail. Here, say the four sentences and move on."
  );
}

function beanPostProcessor() {
  const s = light();
  title(s, "The seam: something may replace your bean", "One method, and everything else follows");
  code(s, [
    "public interface BeanPostProcessor {",
    "",
    "    default Object postProcessAfterInitialization(",
    "            Object bean, String beanName) {",
    "        return bean;          // ...or something else entirely",
    "    }",
    "}",
  ], { x: 0.5, y: 1.5, w: 9, h: 1.95, size: 13 });
  bullets(s, [
    "The container creates the post-processors first, then passes every other bean through them",
    "A processor may return a different object — that one line is what makes AOP possible",
    "It runs after @PostConstruct, so a proxy always wraps a fully initialised bean",
  ], { y: 3.55, h: 1.15, size: 14 });
  callout(s, "Everything in this talk is built on this single method.", { y: 4.75 });
  s.addNotes(
    "Slow down on the comment in the code: 'or something else entirely'. That is the whole idea, and people miss it.\n\n" +
    "Spring has the same interface, with a before and an after method, and dozens of implementations. Two of them are AutowiredAnnotationBeanPostProcessor and the AOP auto-proxy creator.\n\n" +
    "The ordering point is worth ten seconds: if post-processing ran before @PostConstruct, your initialisation method would run on a proxy, and a transaction could be opened around it."
  );
}

function mappingTableAdvanced() {
  const s = light();
  title(s, "Who reads what, part two", "The annotations that need more than a definition");
  table(s, [
    header(["Annotation", "Read by, in minispring", "The Spring class", "In XML, before"]),
    [mono9("@Transactional"), mono9("TransactionalProcessor"), mono9("TransactionInterceptor"), mono9("<tx:advice> + <aop:config>")],
    [mono9("@RestController"), mono9("DispatcherServer"), mono9("RequestMappingHandlerMapping"), mono9("<mvc:annotation-driven>")],
    [mono9("ApplicationListener"), mono9("ApplicationEventMulticaster"), mono9("SimpleApplicationEvent...caster"), mono9("a <bean> implementing it")],
    [{ text: "@Conditional", options: { fontFace: MONO, fontSize: 9, bold: true, color: ACCENT } }, mono9("ConditionEvaluator"), mono9("ConditionEvaluator (same name)"), { text: "no equivalent", options: { color: WARN, bold: true, fontSize: 9 } }],
  ], { y: 1.7, colW: [2.1, 2.1, 2.8, 2.0], size: 9 });
  callout(s, "The last row is why Spring Boot could not have been built in the XML era.", { y: 4.3, color: WARN });
  s.addNotes(
    CUT + "it is a map, not an argument; the proxy slides carry the section on their own.\n\n" +
    "The punchline is the last row, and it is the bridge to the auto-configuration part. XML could describe beans; it could not describe a decision that depends on the classpath and on what you already declared.\n\n" +
    "If someone objects that XML had profiles: true, and profiles are the closest ancestor of @Conditional."
  );
}

function selfInvocation() {
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
    "Say the workarounds briefly: split into two beans, inject the bean into itself, or use AopContext. The first one is the honest fix.\n\n" +
    "This is the story that got the most reactions on my LinkedIn post. It is relatable, not academic."
  );
}

function cglib() {
  const s = light();
  title(s, "Spring's other proxy: a generated subclass", "Same idea, different trick — and it is what makes @Configuration work");
  code(s, [
    "OrderServiceImpl$$SpringCGLIB$$0  extends  OrderServiceImpl",
  ], { x: 0.5, y: 1.5, w: 9, h: 0.6, size: 13 });
  table(s, [
    header(["", "JDK dynamic proxy (minispring)", "CGLIB subclass (Spring Boot default)"]),
    ["How", "implements the interfaces", "extends the class, overrides its methods"],
    ["Requires", "an interface", "a class and methods that are not final"],
    [mono("getBean(Impl.class)"), "fails: the proxy is not that type", "works: the proxy is a subclass"],
    ["Built by", "the JDK", "bytecode generated at run time, with ASM"],
    ["Self-invocation", "bypassed", "bypassed too — nothing changes"],
  ], { y: 2.25, colW: [1.9, 3.4, 3.7], size: 11 });
  callout(s, "It also rewrites @Configuration classes, so one @Bean method calling another returns the singleton. That is the one thing minispring cannot do.", { y: 4.55 });
  s.addNotes(
    "This slide answers the question someone always asks after the proxy section: Spring proxies classes that have no interface, so how?\n\n" +
    "Explain the mechanism in one sentence: CGLIB writes a new class that extends yours and overrides every method to call the interceptor first.\n\n" +
    "Then the consequences, in order: no interface needed; a final class or a final, private or static method is silently never intercepted; getBean on the implementation class works because the proxy really is one; and the proxy is built without calling your constructor, using Objenesis, so its own fields stay null — it is only a switchboard in front of the real bean.\n\n" +
    "Three facts for questions: CGLIB has been repackaged inside spring-core since Spring 3.2; it is built on ASM; and it does not work in a GraalVM native image, which is part of why Spring 6 introduced AOT processing."
  );
}

function timed() {
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

function twoRules() {
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
    "These two rules are the answer to the question that started this part, and the takeaway of the whole section.\n\n" +
    "Rule two explains the warning in Spring's own documentation: @ConditionalOnMissingBean is only reliable inside an auto-configuration. Now they know why, instead of just obeying.\n\n" +
    "Bolt: @ConditionalOnClass has to name classes as strings in my version, because reading a Class attribute pointing at an absent class fails — which is exactly the case it exists for. Spring solves it by reading annotations from the bytecode with ASM. One constraint, and a dependency people never question suddenly makes sense."
  );
}

function conditionsFamily() {
  const s = light();
  title(s, "The @ConditionalOnX family", "Each one is an ordinary @Conditional with a name");
  table(s, [
    header(["Annotation", "Registers the bean when", "What it is for"]),
    [mono9("@ConditionalOnClass(name=)"), "every named class is on the classpath", "a starter stays quiet without its library"],
    [mono9("@ConditionalOnMissingClass"), "none of them is", "fall back to another implementation"],
    [mono9("@ConditionalOnBean(X.class)"), "a bean of that type is already there", "complete what the application started"],
    [mono9("@ConditionalOnMissingBean"), "no bean of that type exists yet", "provide the default, and step aside"],
    [mono9("@ConditionalOnProperty"), "a property says so", "let people switch you off"],
  ], { y: 1.7, colW: [2.6, 3.4, 3.0], size: 10 });
  callout(s, "The container knows none of them. Writing your own condition takes about a dozen lines.", { y: 4.3 });
  s.addNotes(
    CUT + "the two rules slide already carries the idea; this is the reference version.\n\n" +
    "The point to make: these five cover almost everything Spring Boot does. When you read a starter's source, you will recognise all of them.\n\n" +
    "The Condition interface itself returns a decision plus a reason, which is what makes the next slide possible."
  );
}

function report() {
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
    "DEMO: run the three-scenario demo from the repository. Default bean, your own bean, starter switched off. Ninety seconds."
  );
}

function costTable() {
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
    "If someone asks how to make Spring Boot start faster: lazy initialisation, fewer starters, and AOT with native images. One sentence, do not start a second talk.\n\n" +
    "Own my own limitations too: constructor injection only, JDK proxies only, no jar scanning, not thread-safe during startup. They are choices, and they are in the README."
  );
}

function numbers() {
  const s = light();
  title(s, "The whole thing, in numbers", "So you know what 'small' means here");
  const stats = [
    ["1,500", "lines, both modules"],
    ["0", "runtime dependencies"],
    ["129", "tests, no mocks"],
    ["92%", "coverage, gate at 90%"],
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
    CUT + "the numbers are nice, not necessary.\n\n" +
    "Use them to set expectations, not to brag. If someone asks about the tests: JUnit 5 and AssertJ only, no Mockito. When a test needs a mock, the design is too coupled — that rule alone improved the container's design twice.\n\n" +
    "SonarCloud reports zero bugs, zero vulnerabilities and zero code smells, and the build fails below 90% coverage. Say it once, then stop."
  );
}

/* ================= composition ================= */

async function buildDeckOne() {
  newDeck("Spring is not magic (1/2) — how a bean is born");

  deckTitleSlide("part 1 of 2", "How a bean is born", "Scanning, injection, scopes, lifecycle — the whole path from @Component to your object", 45)
    .addNotes(
      "Welcome. Set the contract in one sentence: by the end you will have seen everything that happens between writing @Component and holding the object.\n\n" +
      "Say that this is part one of two, and what part two covers: proxies, @Transactional and auto-configuration. Nobody should feel they are missing half a story.\n\n" +
      "Say who you are in ten seconds. The credibility comes from the code."
    );

  agenda([
    ["Spring, then Spring Boot", "WAR, JAR, who owns the server"],
    ["Why injection at all", "who chooses the implementation"],
    ["What an annotation really is", "bytecode, javap, reflection"],
    ["Who reads which annotation", "minispring, Spring, and XML"],
    ["Creating the bean", "constructor, scopes, lifecycle, cycles"],
    ["Takeaways", "and what part 2 covers"],
  ], "Six steps, from what Spring is to a fully built bean",
    "Do not read the agenda out loud. Point at two things: the javap demo, and the error messages at the end.\n\n" +
    "Tell them about the lightning symbol in the notes: deep dives for people who already know Spring. Beginners can let those wash over them."
  );

  hook();

  section(1, "Spring, then Spring Boot", "What actually changed between the two",
    "Five minutes, no more. Many juniors have never seen a WAR file; many seniors deployed hundreds. Frame it as ownership: who starts whom.");
  timeline();
  diagramSlide("d3-war-vs-jar.png",
    "Say it as a reversal: before, Tomcat started and then loaded your application. Now your application starts, and then starts Tomcat. Everything else follows: one app per JVM, one container image, deploy by running a process.\n\n" +
    "If you have time for one anecdote, tell the story of a shared Tomcat with five WARs where one memory leak took down all five."
  );

  section(2, "Why injection at all", "The problem before the solution",
    "Seven minutes. This is the part I added after a talk where people understood the mechanism but not the point. Do not skip it.");
  wiringByHand();
  whatYouGain();
  containerIsMap();

  section(3, "What an annotation really is", "The part nobody explains, and everything depends on",
    "Twelve minutes, and the heart of the talk. Slow down: this is where beginners either get it or lose the thread.\n\nThe sentence to repeat three times: an annotation does nothing, something has to read it.");
  componentSource();
  diagramSlide("d2-annotation.png",
    "Walk the three boxes left to right, one sentence each: the sticker, the class file, the reader.\n\n" +
    "The metaphor to use out loud: the annotation is a sticker on a box, reflection is the pair of glasses, the container is the person who acts on what is written. Remove any of the three and nothing happens.\n\n" +
    "The red line at the bottom is the demo you are about to run."
  );
  javapDemo();
  retentionTable();
  reflectionApi();

  section(4, "Who reads which annotation", "minispring, Spring, and the XML we used to write",
    "Fifteen minutes. The table on the next slide is the one people photograph — pause and say so.\n\nThen two zooms only, and the three slides that close the story: scopes, lifecycle, cycles.");
  mappingTableCreation();
  threeWays();
  zoomScanner();
  zoomConstructor();
  scopes();
  lifecycle();
  circularDependencies();
  diagramSlide("d1-lifecycle.png",
    "Recap slide. Walk it once, quickly, naming the annotation that matters at each step.\n\n" +
    "Then point at steps 7 and 8, and say: this is where part 2 starts. The bean is built, and something is about to replace it.\n\n" +
    CUT + "it repeats what they just saw."
  );

  takeawaysSlide("Three things to take home", [
    ["An annotation does nothing", "Something has to read it. javap showed you exactly what that something reads."],
    ["A container is a Map and a few rules", "Definitions first, instances second. Every feature follows that order."],
    ["Constructor injection is a design choice", "It makes your dependencies visible, and your cycles impossible to ignore."],
  ], "github.com/ELMAALMIA/minispring", "Part 2: why your @Transactional sometimes does nothing, and how @Conditional decides.",
    "Close on the three sentences, then stop talking.\n\n" +
    "The call to action that works: clone it and read the commit history in order, one concept per commit.\n\n" +
    "Announce part 2 in one sentence, then open the floor. Expected questions: field injection, why no CGLIB, is it production-ready (no, and say so)."
  );

  await pres.writeFile({ fileName: OUT_DIR + "/minispring-talk-1-beans.pptx" });
  console.log("written: minispring-talk-1-beans.pptx");
}

async function buildDeckTwo() {
  newDeck("Spring is not magic (2/2) — what happens to your bean");

  deckTitleSlide("part 2 of 2", "What happens to your bean", "Post-processors, proxies, @Transactional, and how auto-configuration decides", 45)
    .addNotes(
      "Open by naming the two questions this hour answers: why does @Transactional sometimes do nothing, and why is that bean not there.\n\n" +
      "Say that part 1 is not required: the next two slides catch everyone up.\n\n" +
      "If some of the room saw part 1, thank them, then go anyway — repetition costs you two minutes and buys everyone else the whole talk."
    );

  diagramSlide("d1-lifecycle.png",
    "The recap in one picture. Walk steps 1 to 6 quickly: scan, describe, create, inject, initialise.\n\n" +
    "Then stop at step 7 and say: this is where today starts. The bean exists, it is initialised, and something is about to be allowed to replace it."
  );
  recapRules();

  section(1, "The seam", "One method, and everything else is built on it",
    "Five minutes. Do not rush this: without the post-processor, the proxy slides look like magic again.");
  beanPostProcessor();
  mappingTableAdvanced();

  section(2, "Proxies", "The container does not always hand you your bean",
    "Twelve minutes, and the emotional peak of the talk. The self-invocation slide is what people will tell their colleagues about.");
  diagramSlide("d4-proxy.png",
    "Build the picture in words before the audience reads it: the caller does not hold your bean, it holds a proxy that implements the same interface.\n\n" +
    "The post-processor is 40 lines: find the @Transactional methods, ask the JDK for a proxy, call the transaction manager around each call.\n\n" +
    "Then pause, because the next slide is the one people will remember."
  );
  selfInvocation();
  cglib();

  section(3, "Invent your own annotation", "The moment this stops being someone else's framework",
    "Eight minutes. This is the part that converts spectators into people who will try it tonight.\n\nIf the room is with you, do it live in the IDE.");
  timed();

  section(4, "Auto-configuration", "How @Conditional decides what gets wired",
    "Ten minutes. Tell the true story: this part exists because someone asked the question in a LinkedIn comment. It makes the talk feel alive.");
  diagramSlide("d5-autoconfig.png",
    "Walk the four boxes. Insist on the first one: your beans are registered first. Everything else depends on that ordering.\n\n" +
    "Say what auto-configuration is in one sentence: a plugin that contributes bean definitions through an extension point, after you, and only for what is missing.\n\n" +
    "In my implementation the container knows nothing about it: a separate module, 300 lines."
  );
  twoRules();
  conditionsFamily();
  report();

  section(5, "What it costs", "The honest slide",
    "Three minutes. Never end a rebuild talk on a victory lap — the audience knows Spring does infinitely more. Owning that is what makes the rest credible.");
  costTable();
  numbers();

  takeawaysSlide("Three things to take home", [
    ["A post-processor may hand you a different object", "That single method is where @Transactional, @Cacheable and @Async come from."],
    ["Self-invocation bypasses the proxy", "Inside the bean, this is the bean. No proxy, no transaction, no warning."],
    ["Conditions read definitions, and run last", "That is the only reason \"unless you declared your own\" can work."],
  ], "github.com/ELMAALMIA/minispring", "Read it commit by commit: one concept each, all green.",
    "Close on the three sentences, then stop talking. Do not add a fourth point.\n\n" +
    "Then open the floor. Expected questions: why no CGLIB in your container; does @Transactional work on private methods (no, and now they know why); is it production-ready (no); how long did it take."
  );

  await pres.writeFile({ fileName: OUT_DIR + "/minispring-talk-2-proxies.pptx" });
  console.log("written: minispring-talk-2-proxies.pptx");
}

buildDeckOne()
  .then(buildDeckTwo)
  .catch((error) => {
    console.error(error);
    process.exitCode = 1;
  });
