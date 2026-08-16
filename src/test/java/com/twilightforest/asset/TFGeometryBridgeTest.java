package com.twilightforest.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.function.Consumer;

class TFGeometryBridgeTest {

	private static final String MODEL = "tf/test/FakeModel";
	private static final String RENDERER = "tf/test/FakeRenderer";
	private static final String RENDERER_DESC = "L" + RENDERER + ";";

	@Test
	void spiderConvertsToBtasOwnGeometry() {
		String json = TFGeometryBridge.toGeometryJson("spider", TFGeometryBridge.spiderBase(), 64, 32);

		assertTrue(json.contains("\"pivot\": [0, 9, -3]"), "spider head pivot");
		assertTrue(json.contains("{\"origin\": [-4, 5, -11], \"size\": [8, 8, 8], \"uv\": [32, 4]}"),
			"spider head box");

		assertTrue(json.contains("\"pivot\": [0, 9, 0]"), "spider neck pivot");
		assertTrue(json.contains("{\"origin\": [-3, 6, -3], \"size\": [6, 6, 6], \"uv\": [0, 0]}"),
			"spider neck box");

		assertTrue(json.contains("\"pivot\": [0, 9, 9]"), "spider body pivot");
		assertTrue(json.contains("{\"origin\": [-5, 5, 3], \"size\": [10, 8, 12], \"uv\": [0, 12]}"),
			"spider body box");
	}

	@Test
	void countedLoopIsUnrolledWithItsCounter() {
		byte[] bytes = model(ctor -> {

			newRenderer(ctor, "first", 4, 5);
			addBox(ctor, "first", -1.0F, -2.0F, -3.0F, 2, 3, 4);
			setRotationPoint(ctor, "first", 0.0F, 1.0F, 2.0F);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitInsn(Opcodes.ICONST_2);
			ctor.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
			ctor.visitInsn(Opcodes.DUP);
			ctor.visitInsn(Opcodes.ICONST_0);
			ctor.visitIntInsn(Opcodes.BIPUSH, 7);
			ctor.visitInsn(Opcodes.IASTORE);
			ctor.visitInsn(Opcodes.DUP);
			ctor.visitInsn(Opcodes.ICONST_1);
			ctor.visitIntInsn(Opcodes.BIPUSH, 9);
			ctor.visitInsn(Opcodes.IASTORE);
			ctor.visitFieldInsn(Opcodes.PUTFIELD, MODEL, "order", "[I");

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitInsn(Opcodes.ICONST_3);
			ctor.visitTypeInsn(Opcodes.ANEWARRAY, RENDERER);
			ctor.visitFieldInsn(Opcodes.PUTFIELD, MODEL, "parts", "[" + RENDERER_DESC);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitInsn(Opcodes.ICONST_3);
			ctor.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
			ctor.visitFieldInsn(Opcodes.PUTFIELD, MODEL, "flags", "[Z");

			Label test = new Label();
			Label end = new Label();
			ctor.visitInsn(Opcodes.ICONST_0);
			ctor.visitVarInsn(Opcodes.ISTORE, 1);
			ctor.visitLabel(test);
			ctor.visitVarInsn(Opcodes.ILOAD, 1);
			ctor.visitInsn(Opcodes.ICONST_3);
			ctor.visitJumpInsn(Opcodes.IF_ICMPGE, end);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitFieldInsn(Opcodes.GETFIELD, MODEL, "parts", "[" + RENDERER_DESC);
			ctor.visitVarInsn(Opcodes.ILOAD, 1);
			ctor.visitTypeInsn(Opcodes.NEW, RENDERER);
			ctor.visitInsn(Opcodes.DUP);
			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitInsn(Opcodes.ICONST_0);
			ctor.visitIntInsn(Opcodes.BIPUSH, 10);
			ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, RENDERER, "<init>", "(L" + MODEL + ";II)V", false);
			ctor.visitInsn(Opcodes.AASTORE);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitFieldInsn(Opcodes.GETFIELD, MODEL, "parts", "[" + RENDERER_DESC);
			ctor.visitVarInsn(Opcodes.ILOAD, 1);
			ctor.visitInsn(Opcodes.AALOAD);
			ctor.visitVarInsn(Opcodes.ILOAD, 1);
			ctor.visitInsn(Opcodes.I2F);
			ctor.visitInsn(Opcodes.FCONST_0);
			ctor.visitInsn(Opcodes.FCONST_0);
			ctor.visitInsn(Opcodes.ICONST_1);
			ctor.visitInsn(Opcodes.ICONST_1);
			ctor.visitInsn(Opcodes.ICONST_1);
			ctor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, RENDERER, "a", "(FFFIII)V", false);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitFieldInsn(Opcodes.GETFIELD, MODEL, "flags", "[Z");
			ctor.visitVarInsn(Opcodes.ILOAD, 1);
			ctor.visitInsn(Opcodes.ICONST_0);
			ctor.visitInsn(Opcodes.BASTORE);

			ctor.visitIincInsn(1, 1);
			ctor.visitJumpInsn(Opcodes.GOTO, test);
			ctor.visitLabel(end);
		});

		TFGeometryBridge.Extraction extraction = extract(bytes);
		assertEquals(List.of(), extraction.problems, "a counted loop is not a reason to skip a model");

		List<TFGeometryBridge.Bone> bones = extraction.bones;
		assertEquals(4, bones.size(), "one bone before the arrays and three built inside the loop");

		TFGeometryBridge.Bone first = bones.get(0);
		assertEquals("first", first.slot);
		assertEquals(4, first.u);
		assertEquals(5, first.v);
		assertEquals(0.0F, first.pointX);
		assertEquals(1.0F, first.pointY);
		assertEquals(2.0F, first.pointZ);
		assertEquals(1, first.cubes.size());
		assertEquals(-1.0F, first.cubes.get(0).offX);
		assertEquals(2, first.cubes.get(0).width);

		for (int i = 0; i < 3; i++) {
			TFGeometryBridge.Bone part = bones.get(1 + i);
			assertEquals("parts[" + i + "]", part.slot, "an array-held bone is named by field and index");
			assertEquals(0, part.u);
			assertEquals(10, part.v);
			assertEquals(1, part.cubes.size());

			assertEquals((float) i, part.cubes.get(0).offX, "loop body reads the loop counter");
		}
	}

	@Test
	void aLoopWithNoExitIsReportedRatherThanTruncated() {
		byte[] bytes = model(ctor -> {
			newRenderer(ctor, "first", 0, 0);
			Label spin = new Label();
			ctor.visitLabel(spin);
			ctor.visitJumpInsn(Opcodes.GOTO, spin);
		});

		TFGeometryBridge.Extraction extraction = extract(bytes);
		assertFalse(extraction.problems.isEmpty(), "an unbounded loop must fail the model, not shorten it");
		assertTrue(extraction.problems.get(0).contains("still running"),
			"the report has to say what happened: " + extraction.problems);
	}

	@Test
	void aPrivateRotationHelperIsFollowed() {
		byte[] bytes = model(ctor -> {
			newRenderer(ctor, "first", 0, 0);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitFieldInsn(Opcodes.GETFIELD, MODEL, "first", RENDERER_DESC);
			ctor.visitLdcInsn(1.5707964F);
			ctor.visitInsn(Opcodes.FCONST_0);
			ctor.visitInsn(Opcodes.FCONST_0);
			ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, MODEL, "setRotation",
				"(" + RENDERER_DESC + "FFF)V", false);
		}, cw -> {
			MethodVisitor helper = cw.visitMethod(Opcodes.ACC_PRIVATE, "setRotation",
				"(" + RENDERER_DESC + "FFF)V", null, null);
			helper.visitCode();
			writeAngle(helper, "f", 2);
			writeAngle(helper, "g", 3);
			writeAngle(helper, "h", 4);
			helper.visitInsn(Opcodes.RETURN);
			helper.visitMaxs(3, 5);
			helper.visitEnd();
		});

		TFGeometryBridge.Extraction extraction = extract(bytes);
		assertEquals(List.of(), extraction.problems);
		assertEquals(1.5707964F, extraction.bones.get(0).angleX, 1.0E-6F);
		assertEquals(0.0F, extraction.bones.get(0).angleY);
		assertEquals(0.0F, extraction.bones.get(0).angleZ);
	}

	@Test
	void aNamedBoxTakesTheModelsOwnTextureOffset() {
		byte[] bytes = model(ctor -> {

			newRenderer(ctor, "first", 1, 2);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitLdcInsn("head.horn");
			ctor.visitIntInsn(Opcodes.BIPUSH, 20);
			ctor.visitIntInsn(Opcodes.BIPUSH, 30);
			ctor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MODEL, "a", "(Ljava/lang/String;II)V", false);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitTypeInsn(Opcodes.NEW, RENDERER);
			ctor.visitInsn(Opcodes.DUP);
			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitLdcInsn("head");
			ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, RENDERER, "<init>",
				"(L" + MODEL + ";Ljava/lang/String;)V", false);
			ctor.visitFieldInsn(Opcodes.PUTFIELD, MODEL, "head", RENDERER_DESC);

			ctor.visitVarInsn(Opcodes.ALOAD, 0);
			ctor.visitFieldInsn(Opcodes.GETFIELD, MODEL, "head", RENDERER_DESC);
			ctor.visitLdcInsn("horn");
			ctor.visitInsn(Opcodes.FCONST_0);
			ctor.visitInsn(Opcodes.FCONST_0);
			ctor.visitInsn(Opcodes.FCONST_0);
			ctor.visitInsn(Opcodes.ICONST_1);
			ctor.visitInsn(Opcodes.ICONST_1);
			ctor.visitInsn(Opcodes.ICONST_1);
			ctor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, RENDERER, "a",
				"(Ljava/lang/String;FFFIII)V", false);

			addBox(ctor, "head", 0.0F, 0.0F, 0.0F, 2, 2, 2);
		});

		TFGeometryBridge.Extraction extraction = extract(bytes);
		assertEquals(List.of(), extraction.problems);
		assertEquals(2, extraction.bones.size());

		TFGeometryBridge.Bone head = extraction.bones.get(1);
		assertEquals("head", head.slot);
		assertEquals(2, head.cubes.size());
		assertEquals(20, head.cubes.get(0).u, "named box takes the model's registered offset");
		assertEquals(30, head.cubes.get(0).v);
		assertEquals(20, head.cubes.get(1).u, "and the offset persists onto the next box, as Minecraft's does");
		assertEquals(30, head.cubes.get(1).v);
	}

	@Test
	void aPoseMethodWithATrailingEntityIsRead() {
		byte[] bytes = model(ctor -> newRenderer(ctor, "first", 0, 0), cw -> {
			MethodVisitor pose = cw.visitMethod(Opcodes.ACC_PUBLIC, "a",
				"(FFFFFFLjava/lang/Object;)V", null, null);
			pose.visitCode();
			pose.visitVarInsn(Opcodes.ALOAD, 0);
			pose.visitFieldInsn(Opcodes.GETFIELD, MODEL, "first", RENDERER_DESC);
			pose.visitLdcInsn(0.7853982F);
			pose.visitFieldInsn(Opcodes.PUTFIELD, RENDERER, "f", "F");
			pose.visitInsn(Opcodes.RETURN);
			pose.visitMaxs(3, 8);
			pose.visitEnd();

			MethodVisitor render = cw.visitMethod(Opcodes.ACC_PUBLIC, "b",
				"(Ljava/lang/Object;FFFFFF)V", null, null);
			render.visitCode();
			render.visitVarInsn(Opcodes.ALOAD, 0);
			render.visitFieldInsn(Opcodes.GETFIELD, MODEL, "first", RENDERER_DESC);
			render.visitLdcInsn(3.0F);
			render.visitInsn(Opcodes.FCONST_0);
			render.visitInsn(Opcodes.FCONST_0);
			render.visitMethodInsn(Opcodes.INVOKEVIRTUAL, RENDERER, "a", "(FFF)V", false);
			render.visitInsn(Opcodes.RETURN);
			render.visitMaxs(5, 8);
			render.visitEnd();
		});

		TFGeometryBridge.Extraction extraction = extract(bytes);
		assertEquals(List.of(), extraction.problems);
		assertEquals(0.7853982F, extraction.bones.get(0).angleX, 1.0E-6F);
		assertEquals(0.0F, extraction.bones.get(0).pointX,
			"render() sets a rotation point too, and must not be mistaken for a pose method");
	}

	@Test
	void aWildcardSlotCoversEveryElementOfAnArray() {
		TFGeometryBridge.ModelEntry entry = new TFGeometryBridge.ModelEntry();
		entry.renames.put("segments[*]", null);
		entry.renames.put("head", "head");

		assertEquals("segments[*]", TFGeometryBridge.renameKey(entry, "segments[0]"));
		assertEquals("segments[*]", TFGeometryBridge.renameKey(entry, "segments[15]"));
		assertEquals("head", TFGeometryBridge.renameKey(entry, "head"));
		assertNull(TFGeometryBridge.renameKey(entry, "other[0]"), "a wildcard covers only its own field");
		assertNull(TFGeometryBridge.renameKey(entry, "segments"), "and only the indexed form");
	}

	private static TFGeometryBridge.Extraction extract(byte[] bytes) {
		TFGeometryBridge.Manifest manifest = TFGeometryBridge.manifest();
		assertNotNull(manifest.rendererFields.get("f"),
			"the manifest has to be on the test classpath; without it no field means anything");
		return TFGeometryBridge.extract(bytes, manifest, new double[0]);
	}

	private static byte[] model(Consumer<MethodVisitor> constructorBody) {
		return model(constructorBody, cw -> { });
	}

	private static byte[] model(Consumer<MethodVisitor> constructorBody, Consumer<ClassWriter> extras) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, MODEL, null, "java/lang/Object", null);
		cw.visitField(0, "first", RENDERER_DESC, null, null).visitEnd();
		cw.visitField(0, "head", RENDERER_DESC, null, null).visitEnd();
		cw.visitField(0, "parts", "[" + RENDERER_DESC, null, null).visitEnd();
		cw.visitField(0, "flags", "[Z", null, null).visitEnd();
		cw.visitField(0, "order", "[I", null, null).visitEnd();

		MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		ctor.visitCode();
		ctor.visitVarInsn(Opcodes.ALOAD, 0);
		ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		constructorBody.accept(ctor);
		ctor.visitInsn(Opcodes.RETURN);
		ctor.visitMaxs(0, 0);
		ctor.visitEnd();

		extras.accept(cw);
		cw.visitEnd();
		return cw.toByteArray();
	}

	private static void newRenderer(MethodVisitor mv, String field, int u, int v) {
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitTypeInsn(Opcodes.NEW, RENDERER);
		mv.visitInsn(Opcodes.DUP);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitIntInsn(Opcodes.BIPUSH, u);
		mv.visitIntInsn(Opcodes.BIPUSH, v);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, RENDERER, "<init>", "(L" + MODEL + ";II)V", false);
		mv.visitFieldInsn(Opcodes.PUTFIELD, MODEL, field, RENDERER_DESC);
	}

	private static void addBox(MethodVisitor mv, String field,
	                           float offX, float offY, float offZ, int w, int h, int d) {
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, MODEL, field, RENDERER_DESC);
		mv.visitLdcInsn(offX);
		mv.visitLdcInsn(offY);
		mv.visitLdcInsn(offZ);
		mv.visitIntInsn(Opcodes.BIPUSH, w);
		mv.visitIntInsn(Opcodes.BIPUSH, h);
		mv.visitIntInsn(Opcodes.BIPUSH, d);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, RENDERER, "a", "(FFFIII)V", false);
	}

	private static void setRotationPoint(MethodVisitor mv, String field, float x, float y, float z) {
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, MODEL, field, RENDERER_DESC);
		mv.visitLdcInsn(x);
		mv.visitLdcInsn(y);
		mv.visitLdcInsn(z);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, RENDERER, "a", "(FFF)V", false);
	}

	private static void writeAngle(MethodVisitor mv, String field, int local) {
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitVarInsn(Opcodes.FLOAD, local);
		mv.visitFieldInsn(Opcodes.PUTFIELD, RENDERER, field, "F");
	}
}
