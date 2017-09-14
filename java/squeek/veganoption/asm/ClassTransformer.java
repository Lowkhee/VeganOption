package squeek.veganoption.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import squeek.veganoption.ModInfo;

import org.apache.logging.log4j.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

//import jline.internal.Log;

import static org.objectweb.asm.Opcodes.*;



public class ClassTransformer implements IClassTransformer
{
	private static final Logger Log = LogManager.getLogger(ClassTransformer.class.getCanonicalName());
	// gets set in ASMPlugin.injectData
	public static boolean isEnvObfuscated = false;

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		
		if (transformedName.equals("net.minecraft.block.BlockDynamicLiquid"))
		{
			boolean isObfuscated = !name.equals(transformedName);

			ClassNode classNode = readClassFromBytes(bytes);
			
			//ClassTransformer.findNewObfValues(name, transformedName, bytes, this);

			MethodNode method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "tryFlowInto", isObfuscated ? "(Lajs;Lco;Latl;I)V" : "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)V");
			
			/*
			if (Hooks.onFlowIntoBlock(world, pos, state, flowDecay))
				return;
			*/
			InsnList toInject = new InsnList();
			LabelNode ifNotCanceled = new LabelNode();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 2));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 3));
			toInject.add(new VarInsnNode(Opcodes.ILOAD, 4));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class), "onFlowIntoBlock", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", false));
			toInject.add(new JumpInsnNode(Opcodes.IFEQ, ifNotCanceled));
			toInject.add(new InsnNode(Opcodes.RETURN));
			toInject.add(ifNotCanceled);

			method.instructions.insertBefore(findFirstInstruction(method), toInject);

			return writeClassToBytes(classNode);
		}
		/*else if(transformedName.equals("net.minecraft.tileentity.TileEntity"))
		{
			debug to find changed values
			ClassTransformer.findNewObfValues(name, transformedName, bytes, this);
		}*/
		else if (transformedName.equals("net.minecraft.tileentity.TileEntityPiston"))
		{
			boolean isObfuscated = !name.equals(transformedName);

			ClassNode classNode = readClassFromBytes(bytes);

			//ClassTransformer.findNewObfValues(name, transformedName, bytes, this);
			
			MethodNode method = findMethodNodeOfClass(classNode, isObfuscated ? "F_" : "update", "()V");

			InsnList toInject = new InsnList();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, isObfuscated ? "asc" : "net/minecraft/tileentity/TileEntity", isObfuscated ? "b" : "worldObj", isObfuscated ? "Lajs;" : "Lnet/minecraft/world/World;"));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, isObfuscated ? "asc" : "net/minecraft/tileentity/TileEntity", isObfuscated ? "c" : "pos", isObfuscated ? "Lco;" : "Lnet/minecraft/util/math/BlockPos;"));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, isObfuscated ? "atf" : "net/minecraft/tileentity/TileEntityPiston", isObfuscated ? "j" : "progress", "F"));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, isObfuscated ? "atf" : "net/minecraft/tileentity/TileEntityPiston", isObfuscated ? "g" : "extending", "Z"));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class), "onPistonTileUpdate", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FZ)V", false));

			method.instructions.insertBefore(findFirstInstruction(method), toInject);
			return writeClassToBytes(classNode);
		}
		else if (transformedName.equals("net.minecraft.block.BlockPistonBase"))
		{
			
			boolean isObfuscated = !name.equals(transformedName);
			ClassNode classNode = readClassFromBytes(bytes);
			
			//ClassTransformer.findNewObfValues(name, transformedName, bytes, this);
			
			MethodNode method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "doMove", isObfuscated ? "(Lajs;Lco;Lcv;Z)Z" : "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z");
			
			/*
			Hooks.onPistonTryExtend(world, pos)
			*/
			InsnList toInject = new InsnList();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 2));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 3));
			toInject.add(new VarInsnNode(Opcodes.ILOAD, 4));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class), "onPistonMove", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)V", false));

			method.instructions.insertBefore(findFirstInstruction(method), toInject);

			return writeClassToBytes(classNode);
		}
		else if (transformedName.equals("net.minecraft.world.World"))
		{
			boolean isObfuscated = !name.equals(transformedName);

			ClassNode classNode = readClassFromBytes(bytes);
			
			//ClassTransformer.findNewObfValues(name, transformedName, bytes, this);

			// isFullCube before: "t" and "(Lcm;)Z" Now: "d" and "(Lco;Z)Z"
			MethodNode method = findMethodNodeOfClass(classNode, isObfuscated ? "d" : "isBlockFullCube", isObfuscated ? "(Lco;Z)Z" : "(Lnet/minecraft/util/math/BlockPos;)Z");
			
			LabelNode end = findEndLabel(method);
			
			AbstractInsnNode targetNode = findFirstInstruction(method);

			InsnList toInject = new InsnList();

			/*
			int isBlockFullCube = Hooks.isBlockFullCube(null, 0, 0, 0);
			if (isBlockFullCube != -1)
				return isBlockFullCube != 0;
			*/
			LabelNode varStartLabel = new LabelNode();
			LocalVariableNode localVar = new LocalVariableNode("isBlockFullCube", "I", method.signature, varStartLabel, end, method.maxLocals);
			method.maxLocals++;
			method.localVariables.add(localVar);

			toInject.add(new VarInsnNode(ALOAD, 0)); 					// this
			toInject.add(new VarInsnNode(ALOAD, 1)); 					// pos
			toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "isBlockFullCube", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", false));
			toInject.add(new VarInsnNode(ISTORE, localVar.index));		// isBlockFullCube = Hooks.isBlockFullCube(...)
			toInject.add(varStartLabel);								// variable scope start
			LabelNode label = new LabelNode();							// label if condition is true
			toInject.add(new VarInsnNode(ILOAD, localVar.index));		// isBlockFullCube
			toInject.add(new InsnNode(ICONST_M1));						// -1
			toInject.add(new JumpInsnNode(IF_ICMPEQ, label));			// isBlockFullCube != -1
			LabelNode labelReturnIf = new LabelNode();					// label if second condition is true
			toInject.add(new VarInsnNode(ILOAD, localVar.index));		// isBlockFullCube
			toInject.add(new JumpInsnNode(IFEQ, labelReturnIf));		// isBlockFullCube != 0
			toInject.add(new InsnNode(ICONST_1));						// 1 (true)
			toInject.add(new InsnNode(IRETURN));						// return true;
			toInject.add(labelReturnIf);								// if isBlockFullCube == 0, jump here
			toInject.add(new InsnNode(ICONST_0));						// 0 (false)
			toInject.add(new InsnNode(IRETURN));						// return false;
			toInject.add(label);										// if isBlockFullCube == -1, jump here

			method.instructions.insertBefore(targetNode, toInject);

			return writeClassToBytes(classNode);
		}
		else if (transformedName.equals("net.minecraftforge.fluids.BlockFluidFinite"))
		{
			ClassNode classNode = readClassFromBytes(bytes);
			
			//ClassTransformer.findNewObfValues(name, transformedName, bytes, this);

			MethodNode method = findMethodNodeOfClass(classNode, isEnvObfuscated ? "b" : "updateTick", isEnvObfuscated ? "(Lajs;Lco;Latl;Ljava/util/Random;)V" : "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V");
			
			final String setBlockMethodName = isEnvObfuscated ? "a" : "setBlockState";
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (insn.getOpcode() == Opcodes.ICONST_2 && insn.getNext() != null && insn.getNext().getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode) insn.getNext()).name.equals(setBlockMethodName))
				{
					AbstractInsnNode newInsn = new InsnNode(Opcodes.ICONST_3);
					method.instructions.insert(insn, newInsn);
					method.instructions.remove(insn);
					insn = newInsn;
				}
			}

			return writeClassToBytes(classNode, 0);
		}

		return bytes;
	}

	private ClassNode readClassFromBytes(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		return classNode;
	}

	private byte[] writeClassToBytes(ClassNode classNode)
	{
		//(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) causes crash?
		return writeClassToBytes(classNode, ClassWriter.COMPUTE_MAXS);
	}

	private byte[] writeClassToBytes(ClassNode classNode, int flags)
	{
		//(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) causes crash?
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private MethodNode findMethodNodeOfClass(ClassNode classNode, String methodName, String methodDesc)
	{
		for (MethodNode method : classNode.methods)
		{
			if (method.name.equals(methodName) && method.desc.equals(methodDesc))
			{
				return method;
			}
		}
		return null;
	}

	public AbstractInsnNode getOrFindInstruction(AbstractInsnNode firstInsnToCheck)
	{
		return getOrFindInstruction(firstInsnToCheck, false);
	}

	public AbstractInsnNode getOrFindInstruction(AbstractInsnNode firstInsnToCheck, boolean reverseDirection)
	{
		for (AbstractInsnNode instruction = firstInsnToCheck; instruction != null; instruction = reverseDirection ? instruction.getPrevious() : instruction.getNext())
		{
			if (instruction.getType() != AbstractInsnNode.LABEL && instruction.getType() != AbstractInsnNode.LINE)
				return instruction;
		}
		return null;
	}

	public AbstractInsnNode findFirstInstruction(MethodNode method)
	{
		return getOrFindInstruction(method.instructions.getFirst());
	}
	
	public LabelNode findEndLabel(MethodNode method)
	{
		for (AbstractInsnNode instruction = method.instructions.getLast(); instruction != null; instruction = instruction.getPrevious())
		{
			if (instruction instanceof LabelNode)
				return (LabelNode) instruction;
		}
		return null;
	}
	
	//once world/World is loaded, further transFormedNames must be lowercase 
		//debug - run in debugger then build .jar and run in mod folder (look in latest log file for the same reference number)
		protected static void findNewObfValues(String name, String transformedName, byte[] bytes, ClassTransformer objThis)
		{
			int counter = 0;
			Log.log(ModInfo.debugLevel, "Found: " + transformedName);
			boolean isObfuscated = !name.equals(transformedName);
			ClassNode classNode = objThis.readClassFromBytes(bytes);
			Log.log(squeek.veganoption.ModInfo.debugLevel, "----------------------------------------------------------------Class: " + classNode.name + "-------------------------------------------------------------------");
			Log.log(ModInfo.debugLevel, "-The values -" + (isObfuscated ? "are" : "are not") + "- obfuscated.-");
			
			for (MethodNode method : classNode.methods)
			{
				Log.log(ModInfo.debugLevel, "--------------------------------");
				counter++;
				Log.log(squeek.veganoption.ModInfo.debugLevel, "Method Line:(" + counter + ") >>> Name:" + method.name + " -- Description:" + method.desc + " -- Signature: " + method.signature);
				Log.log(ModInfo.debugLevel, "--------------------------------");
			
				if(method.instructions != null)
					for(int i = 0; i < method.instructions.size(); i++)
						if(method.instructions.get(i).getOpcode() == Opcodes.GETFIELD)
						{
							FieldInsnNode field =  (FieldInsnNode)method.instructions.get(i);
							Log.log(ModInfo.debugLevel, "GetField Line:(" + (i + 1) + ") >>> Name: " + field.name + " -- Description: " + field.desc + " -- Owner: " + field.owner); 
						}
			}
		}
}
