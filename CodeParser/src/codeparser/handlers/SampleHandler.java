package codeparser.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.jface.dialogs.MessageDialog;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import Visitors.AssignmentVisitor;
import Visitors.AttributeAccessVisitor;
import Visitors.MethodInvocationVisitor;
import Visitors.TypeDeclarationVisitor;
import Visitors.VariableDeclarationFragmentVisitor;

public class SampleHandler extends AbstractHandler {

	public static String ProjectName;

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		long time1 = System.currentTimeMillis();

		try {
			deleteAllDocuments();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		// ============================================================
		// WORKSPACE
		// ============================================================

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		IProject[] projects = root.getProjects();

		// ============================================================
		// PROCESS PROJECTS
		// ============================================================

		for (IProject project : projects) {

			int packageCounter = 0;
			int classCounter = 0;
			int attributeCounter = 0;
			int methodCounter = 0;
			int commentCounter = 0;
			int LinesOfCode = 0;
			int localVarCounter = 0;

			int inheritanceCounter = 0;
			int MethodInvocationCounter = 0;
			int AttributeAccessCounter = 0;

			try {

				if (!project.isOpen()) {
					continue;
				}

				if (!project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
					continue;
				}

				// ====================================================
				// JAVA PROJECT
				// ====================================================

				IJavaProject javaProject = JavaCore.create(project);

				ProjectName = project.getName();

				// ====================================================
				// XML ROOT
				// ====================================================

				Element xmlroot = new Element("Project");
				Document doc = new Document(xmlroot);

				xmlroot.setAttribute("ProjectName", javaProject.getElementName());

				// ====================================================
				// METRICS XML
				// ====================================================

				Element xmlcodeMetrics = new Element("Project");
				Document codeMetricsDoc = new Document(xmlcodeMetrics);

				xmlcodeMetrics.setAttribute("ProjectName", javaProject.getElementName());

				Element xmlPackages = new Element("Packages");
				xmlroot.addContent(xmlPackages);

				Element xmlMetrics = new Element("Metrics");
				xmlcodeMetrics.addContent(xmlMetrics);

				Element xmlMetricLOC = new Element("LinesOfCode");
				Element xmlMetricNOP = new Element("NumberOfPackages");
				Element xmlMetricNOC = new Element("NumberOfClasses");
				Element xmlMetricNOA = new Element("NumberOfAttributes");
				Element xmlMetricNOM = new Element("NumberOfMethods");
				Element xmlMetricNOCo = new Element("NumberOfComments");
				Element xmlMetricNOLv = new Element("NumberOfLocalVariables");

				Element xmlMetricNOIn = new Element("NumberOfInheritances");
				Element xmlMetricNOI = new Element("NumberOfInvocations");
				Element xmlMetricNOAc = new Element("NumberOfAccesses");

				xmlMetrics.addContent(xmlMetricLOC);
				xmlMetrics.addContent(xmlMetricNOP);
				xmlMetrics.addContent(xmlMetricNOC);
				xmlMetrics.addContent(xmlMetricNOA);
				xmlMetrics.addContent(xmlMetricNOM);
				xmlMetrics.addContent(xmlMetricNOCo);
				xmlMetrics.addContent(xmlMetricNOLv);

				xmlMetrics.addContent(xmlMetricNOIn);
				xmlMetrics.addContent(xmlMetricNOI);
				xmlMetrics.addContent(xmlMetricNOAc);

				// ====================================================
				// PACKAGES
				// ====================================================

				for (IPackageFragment packageFragment : javaProject.getPackageFragments()) {

					if (packageFragment.getKind() != IPackageFragmentRoot.K_SOURCE) {
						continue;
					}

					if (packageFragment.getElementName().equals("")) {
						continue;
					}

					Element xmlPackage = new Element("Package");

					xmlPackages.addContent(xmlPackage);

					xmlPackage.setAttribute("PackageName", packageFragment.getElementName());

					packageCounter++;

					// =================================================
					// CLASSES
					// =================================================

					Element xmlClasses = new Element("Classes");
					xmlPackage.addContent(xmlClasses);

					for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {

						// =============================================
						// AST PARSER
						// =============================================

						ASTParser parser = ASTParser.newParser(AST.JLS8);

						parser.setSource(compilationUnit);
						parser.setKind(ASTParser.K_COMPILATION_UNIT);

						/*
						 * IMPORTANT:
						 *
						 * Bindings are enabled, but the program NEVER assumes that resolveBinding() is
						 * non-null.
						 */
						parser.setResolveBindings(true);
						parser.setBindingsRecovery(true);
						parser.setStatementsRecovery(true);
						parser.setIgnoreMethodBodies(false);

						CompilationUnit cu = (CompilationUnit) parser.createAST(null);

						// =============================================
						// FIND TYPES
						// =============================================

						TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();

						cu.accept(typeDeclarationVisitor);

						for (TypeDeclaration typeDeclaration : typeDeclarationVisitor.getTypes()) {

							// =========================================
							// CLASS XML
							// =========================================

							Element xmlClass = new Element("Class");
							xmlClasses.addContent(xmlClass);

							String className = typeDeclaration.getName().getFullyQualifiedName();

							xmlClass.setAttribute("ClassName", className);

							classCounter++;

							// =========================================
							// CLASS LOC
							// =========================================

							int classStart = typeDeclaration.getStartPosition();

							int classEnd = classStart + typeDeclaration.getLength();

							int classStartLine = cu.getLineNumber(classStart);

							int classEndLine = cu.getLineNumber(classEnd - 1);

							int classLOC = classEndLine - classStartLine + 1;

							xmlClass.setAttribute("LOC", String.valueOf(classLOC));

							LinesOfCode += classLOC;

							// =========================================
							// TYPE BINDING
							// =========================================

							ITypeBinding typeBinding = typeDeclaration.resolveBinding();

							// =========================================
							// CLASS ACCESS LEVEL
							// =========================================

							if (typeBinding != null) {

								int modifiers = typeBinding.getModifiers();

								if (Modifier.isPublic(modifiers)) {

									xmlClass.setAttribute("classAccessLevel", "public");

								} else if (Modifier.isProtected(modifiers)) {

									xmlClass.setAttribute("classAccessLevel", "protected");

								} else if (Modifier.isPrivate(modifiers)) {

									xmlClass.setAttribute("classAccessLevel", "private");

								} else {

									xmlClass.setAttribute("classAccessLevel", "default");
								}

							} else {

								/*
								 * Binding unavailable. Use AST modifiers instead.
								 */

								int modifiers = typeDeclaration.getModifiers();

								if (Modifier.isPublic(modifiers)) {

									xmlClass.setAttribute("classAccessLevel", "public");

								} else if (Modifier.isProtected(modifiers)) {

									xmlClass.setAttribute("classAccessLevel", "protected");

								} else if (Modifier.isPrivate(modifiers)) {

									xmlClass.setAttribute("classAccessLevel", "private");

								} else {

									xmlClass.setAttribute("classAccessLevel", "default");
								}
							}

							// =========================================
							// INTERFACE
							// =========================================

							xmlClass.setAttribute("isInterface", String.valueOf(typeDeclaration.isInterface()));

							// =====================================================
							// INHERITANCE
							// =====================================================

							if (typeBinding != null) {

								ITypeBinding superclass = typeBinding.getSuperclass();

								if (superclass != null) {

									xmlClass.setAttribute("Superclass", superclass.getName());

									inheritanceCounter++;
								}

							} else {

								/*
								 * Binding unavailable.
								 *
								 * Use the AST superclass instead.
								 */

								if (typeDeclaration.getSuperclassType() != null) {

									xmlClass.setAttribute("Superclass", typeDeclaration.getSuperclassType().toString());

									inheritanceCounter++;
								}
							}

							// =====================================================
							// PACKAGE
							// =====================================================

							if (typeBinding != null && typeBinding.getPackage() != null) {

								xmlClass.setAttribute("DeclaredPackage", typeBinding.getPackage().getName());

							} else {

								xmlClass.setAttribute("DeclaredPackage", packageFragment.getElementName());
							}

							// =====================================================
							// SUPER INTERFACES
							// =====================================================

							Element xmlInterfaces = new Element("SuperInterfaces");

							xmlClass.addContent(xmlInterfaces);

							if (typeBinding != null) {

								ITypeBinding[] interfaces = typeBinding.getInterfaces();

								if (interfaces != null) {

									for (ITypeBinding itf : interfaces) {

										if (itf == null) {
											continue;
										}

										Element xmlInterface = new Element("Interface");

										xmlInterfaces.addContent(xmlInterface);

										xmlInterface.setAttribute("InterfaceName", itf.getName());
									}
								}

							} else {

								/*
								 * Binding unavailable.
								 *
								 * Read interfaces directly from AST.
								 */

								List<?> superInterfaces = typeDeclaration.superInterfaceTypes();

								for (Object obj : superInterfaces) {

									Element xmlInterface = new Element("Interface");

									xmlInterfaces.addContent(xmlInterface);

									xmlInterface.setAttribute("InterfaceName", obj.toString());
								}
							}

							// =====================================================
							// COMPOSITIONS
							//
							// THIS IS DIRECTLY AFTER <SuperInterfaces />
							// =====================================================

							Element xmlCompositions = new Element("Compositions");

							xmlClass.addContent(xmlCompositions);

							// =====================================================
							// COMMENTS
							// =====================================================

							String source = compilationUnit.getSource();

							int classComments = 0;

							Element xmlComments = new Element("Comments");

							xmlClass.addContent(xmlComments);

							if (cu.getCommentList() != null) {

								for (Object obj : cu.getCommentList()) {

									Comment c = (Comment) obj;

									int start = c.getStartPosition();

									int end = start + c.getLength();

									if (start >= 0 && end <= source.length()) {

										String text = source.substring(start, end).replaceAll("[^a-zA-Z0-9 ]", " ")
												.replaceAll("\\s+", " ").trim();

										Element comment = new Element("Comment");

										comment.setAttribute("CommentText", text);

										xmlComments.addContent(comment);

										commentCounter++;
										classComments++;
									}
								}
							}

							xmlClass.setAttribute("NOC", String.valueOf(classComments));

							// =====================================================
							// ATTRIBUTES
							// =====================================================

							Element xmlFields = new Element("Attributes");

							xmlClass.addContent(xmlFields);

							for (FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {

								// =================================================
								// FIELD TYPE
								// =================================================

								String fieldType = fieldDeclaration.getType().toString();

								for (VariableDeclarationFragment variable : (List<VariableDeclarationFragment>) fieldDeclaration
										.fragments()) {

									Element xmlField = new Element("Attribute");

									xmlFields.addContent(xmlField);

									String variableName = variable.getName().getFullyQualifiedName();

									xmlField.setAttribute("AttributeName", variableName);

									attributeCounter++;

									// =============================================
									// VARIABLE BINDING
									// =============================================

									IVariableBinding variableBinding = variable.resolveBinding();

									// =============================================
									// ATTRIBUTE ACCESS LEVEL
									// =============================================

									int fieldModifiers = fieldDeclaration.getModifiers();

									if (Modifier.isPublic(fieldModifiers)) {

										xmlField.setAttribute("AttributeAccessLevel", "public");

									} else if (Modifier.isProtected(fieldModifiers)) {

										xmlField.setAttribute("AttributeAccessLevel", "protected");

									} else if (Modifier.isPrivate(fieldModifiers)) {

										xmlField.setAttribute("AttributeAccessLevel", "private");

									} else {

										xmlField.setAttribute("AttributeAccessLevel", "default");
									}

									// =============================================
									// ATTRIBUTE TYPE
									// =============================================

									xmlField.setAttribute("AttributeType", fieldType);

									xmlField.setAttribute("isStaticAttribute",
											String.valueOf(Modifier.isStatic(fieldModifiers)));

									// =================================================
									// COMPOSITION
									//
									// IMPORTANT:
									// We DO NOT require resolveBinding().
									//
									// Example:
									//
									// private Engine engine;
									//
									// becomes:
									//
									// <Composition
									// ObjectName="engine"
									// ClassName="Engine"/>
									// =================================================

									String compositionClassName = getSimpleTypeName(fieldType);

									if (isCompositionType(fieldType)) {

										Element composition = new Element("Composition");

										composition.setAttribute("ObjectName", variableName);

										composition.setAttribute("ClassName", compositionClassName);

										xmlCompositions.addContent(composition);
									}

									// =============================================
									// IF BINDING EXISTS, USE REAL TYPE
									// =============================================

									if (variableBinding != null) {

										ITypeBinding variableType = variableBinding.getType();

										if (variableType != null) {

											/*
											 * Keep XML type more accurate when binding is available.
											 */

											xmlField.setAttribute("AttributeType", variableType.getName());
										}
									}
								}
							}

							// =====================================================
							// METHODS
							// =====================================================

							Element xmlMethods = new Element("Methods");

							xmlClass.addContent(xmlMethods);

							for (MethodDeclaration methodDeclaration : typeDeclaration.getMethods()) {

								Element xmlMethod = new Element("Method");

								xmlMethods.addContent(xmlMethod);

								String methodName = methodDeclaration.getName().getFullyQualifiedName();

								xmlMethod.setAttribute("MethodName", methodName);

								methodCounter++;

								// =============================================
								// METHOD BINDING
								// =============================================

								IMethodBinding methodBinding = methodDeclaration.resolveBinding();

								// =============================================
								// METHOD ACCESS
								// =============================================

								int methodModifiers = methodDeclaration.getModifiers();

								if (Modifier.isPublic(methodModifiers)) {

									xmlMethod.setAttribute("MethodAccessLevel", "public");

								} else if (Modifier.isProtected(methodModifiers)) {

									xmlMethod.setAttribute("MethodAccessLevel", "protected");

								} else if (Modifier.isPrivate(methodModifiers)) {

									xmlMethod.setAttribute("MethodAccessLevel", "private");

								} else {

									xmlMethod.setAttribute("MethodAccessLevel", "default");
								}

								// =============================================
								// STATIC
								// =============================================

								xmlMethod.setAttribute("isStaticMethod",
										String.valueOf(Modifier.isStatic(methodModifiers)));

								// =============================================
								// RETURN TYPE
								// =============================================

								if (methodBinding != null && methodBinding.getReturnType() != null) {

									xmlMethod.setAttribute("MethodReturnType", methodBinding.getReturnType().getName());

								} else if (methodDeclaration.getReturnType2() != null) {

									xmlMethod.setAttribute("MethodReturnType",
											methodDeclaration.getReturnType2().toString());

								} else {

									/*
									 * Constructor
									 */

									xmlMethod.setAttribute("MethodReturnType", "constructor");
								}

								// =============================================
								// DECLARED CLASS
								// =============================================

								if (methodBinding != null && methodBinding.getDeclaringClass() != null) {

									xmlMethod.setAttribute("DeclaredClass",
											methodBinding.getDeclaringClass().getName());

								} else {

									xmlMethod.setAttribute("DeclaredClass", className);
								}

								// =====================================================
								// PARAMETERS
								// =====================================================

								Element xmlParameters = new Element("Parameters");

								xmlParameters.setAttribute("NumberOfParameters",
										String.valueOf(methodDeclaration.parameters().size()));

								xmlMethod.addContent(xmlParameters);

								for (SingleVariableDeclaration parameter : (List<SingleVariableDeclaration>) methodDeclaration
										.parameters()) {

									Element xmlParameter = new Element("Parameter");

									xmlParameters.addContent(xmlParameter);

									xmlParameter.setAttribute("ParameterName",
											parameter.getName().getFullyQualifiedName());

									String parameterType = parameter.getType().toString();

									xmlParameter.setAttribute("ParameterType", parameterType);

									/*
									 * If binding is available, use its type.
									 */

									if (parameter.resolveBinding() != null
											&& parameter.resolveBinding().getType() != null) {

										xmlParameter.setAttribute("ParameterType",
												parameter.resolveBinding().getType().getName());
									}
								}

								// =====================================================
								// LOCAL VARIABLES
								// =====================================================

								Element xmlVariableDeclarations = new Element("LocalVariables");

								xmlMethod.addContent(xmlVariableDeclarations);

								VariableDeclarationFragmentVisitor variableDeclarationFragmentVisitor = new VariableDeclarationFragmentVisitor();

								methodDeclaration.accept(variableDeclarationFragmentVisitor);

								for (VariableDeclarationFragment variableDeclarationFragment : variableDeclarationFragmentVisitor
										.getVariables()) {

									IVariableBinding localBinding = variableDeclarationFragment.resolveBinding();

									if (localBinding == null) {
										continue;
									}

									Element xmlVariableDeclaration = new Element("LocalVariable");

									xmlVariableDeclarations.addContent(xmlVariableDeclaration);

									xmlVariableDeclaration.setAttribute("LocalVariableName",
											variableDeclarationFragment.getName().toString());

									if (localBinding.getType() != null) {

										xmlVariableDeclaration.setAttribute("LocalVariableType",
												localBinding.getType().getName());
									}

									localVarCounter++;
								}

								// =====================================================
								// ATTRIBUTE ACCESSES
								// =====================================================

								Element xmlFieldDeclarations = new Element("AttributeAccesses");

								xmlMethod.addContent(xmlFieldDeclarations);

								AttributeAccessVisitor fieldDeclarationFragmentVisitor = new AttributeAccessVisitor();

								methodDeclaration.accept(fieldDeclarationFragmentVisitor);

								for (SimpleName simpleName : fieldDeclarationFragmentVisitor.getFields()) {

									if (!(simpleName.resolveBinding() instanceof IVariableBinding)) {
										continue;
									}

									IVariableBinding variableBinding = (IVariableBinding) simpleName.resolveBinding();

									if (variableBinding == null) {
										continue;
									}

									if (variableBinding.getName().contains("out")) {
										continue;
									}

									Element xmlFieldDeclaration = new Element("AttributeAccess");

									xmlFieldDeclarations.addContent(xmlFieldDeclaration);

									xmlFieldDeclaration.setAttribute("AttributeAccessName", variableBinding.getName());

									AttributeAccessCounter++;

									if (variableBinding.getType() != null) {

										xmlFieldDeclaration.setAttribute("AttributeAccessType",
												variableBinding.getType().getName());
									}

									xmlFieldDeclaration.setAttribute("HowIsItUsed", simpleName.getParent().toString());

									xmlFieldDeclaration.setAttribute("AccessedIn", methodName);
								}

								// =====================================================
								// METHOD INVOCATIONS
								// =====================================================

								Element xmlMethodInvocations = new Element("MethodInvocations");

								xmlMethod.addContent(xmlMethodInvocations);

								MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();

								methodDeclaration.accept(methodInvocationVisitor);

								for (MethodInvocation methodInvocation : methodInvocationVisitor.getMethods()) {

									Element xmlMethodInvocation = new Element("MethodInvocation");

									xmlMethodInvocations.addContent(xmlMethodInvocation);

									xmlMethodInvocation.setAttribute("MethodInvocationName",
											methodInvocation.getName().toString());

									MethodInvocationCounter++;

									xmlMethodInvocation.setAttribute("Arguments",
											methodInvocation.arguments().toString());

									xmlMethodInvocation.setAttribute("InvokedBy", methodName);
								}

								// =====================================================
								// ASSIGNMENTS
								// =====================================================

								Element xmlMethodAssignment = new Element("MethodAssignments");

								xmlMethod.addContent(xmlMethodAssignment);

								AssignmentVisitor methodAssignmentVisitor = new AssignmentVisitor();

								methodDeclaration.accept(methodAssignmentVisitor);

								for (Assignment methodAssignment : methodAssignmentVisitor.getAssignments()) {

									Element xmlMethodAssignmentExpression = new Element("Assignment");

									xmlMethodAssignment.addContent(xmlMethodAssignmentExpression);

									xmlMethodAssignmentExpression.setAttribute("LeftHandSide",
											methodAssignment.getLeftHandSide().toString());

									xmlMethodAssignmentExpression.setAttribute("RightHandSide",
											methodAssignment.getRightHandSide().toString());
								}

								// =====================================================
								// METHOD EXCEPTIONS
								// =====================================================

								Element xmlMethodExceptions = new Element("MethodExceptions");

								xmlMethod.addContent(xmlMethodExceptions);

								if (methodBinding != null) {

									ITypeBinding[] exceptionTypes = methodBinding.getExceptionTypes();

									if (exceptionTypes != null) {

										for (ITypeBinding exception : exceptionTypes) {

											if (exception == null) {
												continue;
											}

											Element xmlMethodException = new Element("Exception");

											xmlMethodExceptions.addContent(xmlMethodException);

											xmlMethodException.setAttribute("ExceptionType", exception.getName());
										}
									}

								} else {

									/*
									 * Binding unavailable. Read exceptions directly from AST.
									 */

									List<?> thrownExceptions = methodDeclaration.thrownExceptionTypes();

									for (Object exception : thrownExceptions) {

										Element xmlMethodException = new Element("Exception");

										xmlMethodExceptions.addContent(xmlMethodException);

										xmlMethodException.setAttribute("ExceptionType", exception.toString());
									}
								}
							}
						}
					}
				}

				// ============================================================
				// METRICS
				// ============================================================

				xmlMetricLOC.setAttribute("LOC", String.valueOf(LinesOfCode));

				xmlMetricNOP.setAttribute("NOP", String.valueOf(packageCounter));

				xmlMetricNOC.setAttribute("NOC", String.valueOf(classCounter));

				xmlMetricNOA.setAttribute("NOA", String.valueOf(attributeCounter));

				xmlMetricNOM.setAttribute("NOM", String.valueOf(methodCounter));

				xmlMetricNOCo.setAttribute("NOCo", String.valueOf(commentCounter));

				xmlMetricNOLv.setAttribute("NOLv", String.valueOf(localVarCounter));

				xmlMetricNOIn.setAttribute("NOIn", String.valueOf(inheritanceCounter));

				xmlMetricNOI.setAttribute("NOI", String.valueOf(MethodInvocationCounter));

				xmlMetricNOAc.setAttribute("NOAc", String.valueOf(AttributeAccessCounter));

				// ============================================================
				// OUTPUT DIRECTORY
				// ============================================================

				Bundle bundle = FrameworkUtil.getBundle(SampleHandler.class);

				File pluginFolder = new File(FileLocator.toFileURL(bundle.getEntry("/")).toURI());

				File xmlFolder = new File(pluginFolder, "XML");

				if (!xmlFolder.exists()) {
					xmlFolder.mkdirs();
				}

				File xmlFile = new File(xmlFolder, project.getName() + ".xml");

				File metricsFile = new File(xmlFolder, project.getName() + " - codeMetrics.xml");

				XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

				try (FileOutputStream xmlOutput =
				         new FileOutputStream(xmlFile);
				     FileOutputStream metricsOutput =
				         new FileOutputStream(metricsFile)) {

				    outputter.output(doc, xmlOutput);
				    outputter.output(codeMetricsDoc, metricsOutput);
				}

			} catch (FileNotFoundException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();

			} catch (JavaModelException e) {

				e.printStackTrace();

			} catch (CoreException e) {

				e.printStackTrace();

			} catch (URISyntaxException e) {

				e.printStackTrace();
			}
		}

		// ============================================================
		// EXECUTION TIME
		// ============================================================

		System.out.println("==[The execution time in ms]=========================================");

		long time2 = System.currentTimeMillis();

		System.out.println("The execution time in ms is equal to: " + (time2 - time1));

		System.out.println("=====================================================================");

		MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Code Parser",
				"All done! XML files have been created successfully.");

		return null;
	}

	// ========================================================================
	// GET SIMPLE CLASS NAME
	// ========================================================================

	private static String getSimpleTypeName(String type) {

		if (type == null || type.trim().isEmpty()) {
			return "unknown";
		}

		String result = type.trim();

		// Remove generic part
		int genericIndex = result.indexOf("<");

		if (genericIndex >= 0) {
			result = result.substring(0, genericIndex);
		}

		// Remove array
		while (result.endsWith("[]")) {

			result = result.substring(0, result.length() - 2);
		}

		// Remove package
		int dotIndex = result.lastIndexOf(".");

		if (dotIndex >= 0) {

			result = result.substring(dotIndex + 1);
		}

		return result.trim();
	}

	// ========================================================================
	// DETERMINE WHETHER FIELD IS A COMPOSITION
	// ========================================================================

	private static boolean isCompositionType(String type) {

		if (type == null) {
			return false;
		}

		String cleanType = type.trim();

		/*
		 * Primitive Java types are NOT compositions.
		 */

		if (cleanType.equals("byte") || cleanType.equals("short") || cleanType.equals("int") || cleanType.equals("long")
				|| cleanType.equals("float") || cleanType.equals("double") || cleanType.equals("boolean")
				|| cleanType.equals("char") || cleanType.equals("void")) {

			return false;
		}

		/*
		 * Common Java wrapper classes.
		 */

		if (cleanType.equals("Byte") || cleanType.equals("Short") || cleanType.equals("Integer")
				|| cleanType.equals("Long") || cleanType.equals("Float") || cleanType.equals("Double")
				|| cleanType.equals("Boolean") || cleanType.equals("Character") || cleanType.equals("String")) {

			return false;
		}

		/*
		 * Everything else is treated as an object type.
		 *
		 * Example:
		 *
		 * Engine Address Student Department
		 *
		 * will be considered composition candidates.
		 */

		return true;
	}

	// ========================================================================
	// READ FILE
	// ========================================================================

	public static String readFileToString(String filePath) throws IOException {

		StringBuilder fileData = new StringBuilder(1000);

		BufferedReader reader = new BufferedReader(new FileReader(filePath));

		char[] buf = new char[1024];

		int numRead;

		while ((numRead = reader.read(buf)) != -1) {

			String readData = String.valueOf(buf, 0, numRead);

			fileData.append(readData);
		}

		reader.close();

		return fileData.toString();
	}

	// ========================================================================
	// DELETE XML FILES
	// ========================================================================

	public static void deleteAllDocuments() throws IOException, URISyntaxException {

		Bundle bundle = FrameworkUtil.getBundle(SampleHandler.class);

		File pluginFolder = new File(FileLocator.toFileURL(bundle.getEntry("/")).toURI());

		File xmlFolder = new File(pluginFolder, "XML");

		if (!xmlFolder.exists()) {

			xmlFolder.mkdirs();

			return;
		}

		File[] files = xmlFolder.listFiles();

		if (files != null) {

			for (File file : files) {

				if (file.isFile()) {

					file.delete();
				}
			}
		}
	}
}