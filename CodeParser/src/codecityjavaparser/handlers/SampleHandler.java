package codecityjavaparser.handlers;

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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.ui.handlers.HandlerUtil;

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

import org.eclipse.jface.dialogs.MessageDialog;


public class SampleHandler extends AbstractHandler {

	public static String ProjectName;

	@SuppressWarnings({ "unchecked", "unused" })

	public Object execute(ExecutionEvent event) throws ExecutionException {
		    
		    long time1 = System.currentTimeMillis();
		    
		        try {
		            deleteAllDocuments();
		        } catch (IOException | URISyntaxException e) {
		            e.printStackTrace();
		        }
		    
			// Get the root of the workspace
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			
			
			IProject[] projects = root.getProjects();
			
			
			for (IProject project : projects) {
				
				 int packageCounter=0;
				 int classCounter=0;
				 int attributeCounter=0;
				 int methodCounter=0;
				 int commentCounter=0;
				 int LinesOfCode=0;
				 int localVarCounter=0;
				 
				 int inheritanceCounter=0;
				 int MethodInvocationCounter=0;
				 int AttributeAccessCounter=0;
				 
				 String packageStr="";
				 String classStr="";
				 String attributeStr="";
				 String methodStr="";
				 String commentStr="";
				 String lineStr="";
				 String localVarStr="";
				 
				 String inheritanceStr="";
				 String MethodInvocationStr="";
				 String AttributeAccessStr="";
				 
				try {
					if (project.isNatureEnabled("org.eclipse.jdt.core.javanature") && project.isOpen()) {
						IJavaProject javaProject = JavaCore.create(project);
						Element xmlroot = new Element("Project");
						Element xmlcodeMetrics = new Element("Project");
						
						Document doc = new Document(xmlroot);
						Document codeMetricsDoc = new Document(xmlcodeMetrics);
						
						xmlroot.setAttribute("ProjectName", javaProject.getElementName());
						xmlcodeMetrics.setAttribute("ProjectName", javaProject.getElementName());
						ProjectName=project.getName();
						
						Element xmlPackages = new Element("Packages");
						Element xmlMetrics = new Element("Metrics");
						
						xmlroot.addContent(xmlPackages);
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
						
						for (IPackageFragment packageFragment : javaProject.getPackageFragments()) {
							
							
							if (packageFragment.getKind() == IPackageFragmentRoot.K_SOURCE && !packageFragment.getElementName().equals("")) {
								Element xmlPackage = new Element("Package");
								
								xmlPackages.addContent(xmlPackage);
							
								
								xmlPackage.setAttribute("PackageName", packageFragment.getElementName());
								packageCounter++;
								
						
								
								Element xmlClasses = new Element("Classes");
								xmlPackage.addContent(xmlClasses);
								for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
									   
										ASTParser parser = ASTParser.newParser(AST.JLS4);
										parser.setSource(compilationUnit);
										parser.setKind(ASTParser.K_COMPILATION_UNIT);
										
										parser.setResolveBindings(true);
										parser.setBindingsRecovery(true);
										
										final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
										TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();
										cu.accept(typeDeclarationVisitor);


										for (TypeDeclaration typeDeclaration : typeDeclarationVisitor.getTypes()) {
											Element xmlClass = new Element("Class");
											xmlClasses.addContent(xmlClass);
										
											xmlClass.setAttribute("ClassName", typeDeclaration.getName().getFullyQualifiedName());
											classCounter++;
											
											// ============================================================
										    // Count the lines of code for this class
										    // ============================================================
										    int classStart = typeDeclaration.getStartPosition();
										    int classEnd = classStart + typeDeclaration.getLength();

										    // Convert the AST character positions to source-code line numbers
										    int classStartLine = cu.getLineNumber(classStart);
										    int classEndLine = cu.getLineNumber(classEnd - 1);

										    // Number of physical lines occupied by the class
										    int classLOC = classEndLine - classStartLine + 1;

										    // Store the LOC in the XML Class element
										    xmlClass.setAttribute("LOC", String.valueOf(classLOC));

										    // Add this class's LOC to the project total
										    LinesOfCode += classLOC;

//										    System.out.println("Class: " + typeDeclaration.getName().getFullyQualifiedName() +" | LOC: " +classLOC);

										    // ============================================================
										    // Count the lines of code for this class
										    // ============================================================
											
											// Count the lines of code
									        								       
											
											if (Modifier.isPublic(typeDeclaration.resolveBinding().getModifiers())) {
												xmlClass.setAttribute("classAccessLevel", "public");
											} else
											if (Modifier.isProtected(typeDeclaration.resolveBinding().getModifiers())) {
												xmlClass.setAttribute("classAccessLevel", "protected");
											} else
											if (Modifier.isPrivate(typeDeclaration.resolveBinding().getModifiers())) {
												xmlClass.setAttribute("classAccessLevel", "private");
											}				
											xmlClass.setAttribute("isInterface",String.valueOf(typeDeclaration.isInterface()));
																				
											if (typeDeclaration.resolveBinding().getSuperclass()!=null) {
												xmlClass.setAttribute("Superclass",typeDeclaration.resolveBinding().getSuperclass().getName());
												inheritanceCounter++;
											}
											xmlClass.setAttribute("DeclaredPackage", typeDeclaration.resolveBinding().getPackage().getName());
											Element xmlInterfaces = new Element("SuperInterfaces");
											xmlClass.addContent(xmlInterfaces);
											
											for (ITypeBinding itf : typeDeclaration.resolveBinding().getInterfaces()) {
												Element xmlInterface = new Element("Interface");
												xmlInterfaces.addContent(xmlInterface);
												xmlInterface.setAttribute("InterfaceName",itf.getName());
											}
											
											String packageName0 = packageFragment.getElementName();
											String packageName =packageName0.replace(".", "/");
											
											String source = compilationUnit.getSource();

											int classComments=0;
											
											Element xmlComments = new Element("Comments");
											xmlClass.addContent(xmlComments);

											for (Object obj : cu.getCommentList()) {

											    Comment c = (Comment) obj;

											    int start = c.getStartPosition();
											    int end = start + c.getLength();

											    if (start >= 0 && end <= source.length()) {

//											        String text = source.substring(start, end).replaceFirst("^//","").trim();
//											        String text = source.substring(start, end).replaceAll("[^a-zA-Z0-9]", " ").trim();
											        String text = source.substring(start, end).replaceAll("[^a-zA-Z0-9 ]", " ").replaceAll("\\s+", " ")
											                .trim();
											        Element comment = new Element("Comment");
											        comment.setAttribute("CommentText", text);

											        xmlComments.addContent(comment);
											        commentCounter++;
											        classComments++;
											    }
											}
											
											xmlClass.setAttribute("NOC", String.valueOf(classComments));
											
											
											
											
											Element xmlFields = new Element("Attributes");
											xmlClass.addContent(xmlFields);							
											for (FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {		
												for (VariableDeclarationFragment variable : (List<VariableDeclarationFragment>)fieldDeclaration.fragments()) {
													Element xmlField = new Element("Attribute");
													xmlFields.addContent(xmlField);
													xmlField.setAttribute("AttributeName", variable.getName().getFullyQualifiedName());		
													attributeCounter++;
													
													IVariableBinding variableBinding = variable.resolveBinding();
													if (variableBinding!=null){
													if (Modifier.isPublic(variable.resolveBinding().getModifiers())) {
														xmlField.setAttribute("AttributeAccessLevel", "public");
													} else
													if (Modifier.isProtected(variable.resolveBinding().getModifiers())) {
														xmlField.setAttribute("AttributeAccessLevel", "protected");
													} else
													if (Modifier.isPrivate(variable.resolveBinding().getModifiers())) {
														xmlField.setAttribute("AttributeAccessLevel", "private");
													}
													else xmlField.setAttribute("AttributeAccessLevel", "public");
													
													xmlField.setAttribute("AttributeType", variable.resolveBinding().getType().getName());
													xmlField.setAttribute("isStaticAttribute",String.valueOf(Modifier.isStatic(variable.resolveBinding().getModifiers())));
												}
											}
											}			
											

											Element xmlMethods = new Element("Methods");
											xmlClass.addContent(xmlMethods);
											for (MethodDeclaration methodDeclaration : typeDeclaration.getMethods()) {
												Element xmlMethod = new Element("Method");
												xmlMethods.addContent(xmlMethod);
												xmlMethod.setAttribute("MethodName",methodDeclaration.getName().getFullyQualifiedName());
												methodCounter++;
												
											if (Modifier.
													isPublic(methodDeclaration
													.resolveBinding()
													.getModifiers())) {
												xmlMethod.setAttribute(
														"MethodAccessLevel",
														"public");
											} else if (Modifier
													.isProtected(methodDeclaration
															.resolveBinding()
															.getModifiers())) {
												xmlMethod.setAttribute(
														"MethodAccessLevel",
														"protected");
											} else if (Modifier
													.isPrivate(methodDeclaration
															.resolveBinding()
															.getModifiers())) {
												xmlMethod.setAttribute(
														"MethodAccessLevel",
														"private");
											} else {
												xmlMethod.setAttribute(
														"MethodAccessLevel",
														"null");
											}
												
											
											xmlMethod.setAttribute("MethodReturnType",methodDeclaration.resolveBinding().getReturnType().getName());
											
											xmlMethod.setAttribute("isStaticMethod",String.valueOf(Modifier.isStatic(methodDeclaration.resolveBinding().getModifiers())));

											xmlMethod.setAttribute("DeclaredClass", methodDeclaration.resolveBinding().getDeclaringClass().getName());
																					
									
												Element xmlParameters = new Element("Parameters");
												
												xmlParameters.setAttribute("NumberOfParameters", String.valueOf(methodDeclaration.parameters().size()));
												xmlMethod.addContent(xmlParameters);
												
												for (SingleVariableDeclaration singleVariableDeclaration : (List<SingleVariableDeclaration>)methodDeclaration.parameters()) {
													Element xmlParameter = new Element("Parameter");
													xmlParameters.addContent(xmlParameter);
													
													xmlParameter.setAttribute("ParameterName", singleVariableDeclaration.getName().getFullyQualifiedName());
													xmlParameter.setAttribute("ParameterType", singleVariableDeclaration.resolveBinding().getType().getName());
												}

												
												Element xmlVariableDeclarations = new Element("LocalVariables");
												xmlMethod.addContent(xmlVariableDeclarations);
												VariableDeclarationFragmentVisitor variableDeclarationFragmentVisitor = new VariableDeclarationFragmentVisitor();
												methodDeclaration.accept(variableDeclarationFragmentVisitor);
												for (VariableDeclarationFragment variableDeclarationFragment : variableDeclarationFragmentVisitor.getVariables()) {
												    if(variableDeclarationFragment.resolveBinding()!=null){
												    variableDeclarationFragment.resolveBinding().getName();
													Element xmlVariableDeclaration = new Element("LocalVariable");
													xmlVariableDeclarations.addContent(xmlVariableDeclaration);
													
													xmlVariableDeclaration.setAttribute("LocalVariableName", variableDeclarationFragment.getName().toString());
													xmlVariableDeclaration.setAttribute("LocalVariableType", variableDeclarationFragment.resolveBinding().getType().getName());
													localVarCounter++;
													
//													xmlVariableDeclaration.setAttribute("DeclaredMethod", variableDeclarationFragment.resolveBinding().getDeclaringMethod().getName());
//													xmlVariableDeclaration.setAttribute("DeclaredClass", variableDeclarationFragment.resolveBinding().getDeclaringMethod().getDeclaringClass().getName());

												    }
												}

												Element xmlFieldDeclarations = new Element("AttributeAccesses");
												xmlMethod.addContent(xmlFieldDeclarations);
												AttributeAccessVisitor fieldDeclarationFragmentVisitor = new AttributeAccessVisitor();
												methodDeclaration.accept(fieldDeclarationFragmentVisitor);
												for (SimpleName simpleName : fieldDeclarationFragmentVisitor.getFields()) {
													
													IVariableBinding variableBinding = (IVariableBinding)simpleName.resolveBinding();
													
													if (variableBinding != null){ 
													if(!variableBinding.getName().toString().contains("out")){
														
													Element xmlFieldDeclaration = new Element("AttributeAccess");
													xmlFieldDeclarations.addContent(xmlFieldDeclaration);
																			
													xmlFieldDeclaration.setAttribute("AttributeAccessName", variableBinding.getName().toString());
													
													AttributeAccessCounter++;
													
													xmlFieldDeclaration.setAttribute("AttributeAccessType", variableBinding.getType().getName());
													xmlFieldDeclaration.setAttribute("HowIsItUsed", simpleName.getParent().toString());
													xmlFieldDeclaration.setAttribute("AccessedIn", methodDeclaration.getName().getFullyQualifiedName());

													 }
													}
													}
												
												Element xmlMethodInvocations = new Element("MethodInvocations");
												xmlMethod.addContent(xmlMethodInvocations);
												
												MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
												methodDeclaration.accept(methodInvocationVisitor);
												for (MethodInvocation methodInvocation : methodInvocationVisitor.getMethods()) {
													Element xmlMethodInvocation = new Element("MethodInvocation");
													xmlMethodInvocations.addContent(xmlMethodInvocation);
													
													xmlMethodInvocation.setAttribute("MethodInvocationName", methodInvocation.getName().toString());
													
													MethodInvocationCounter++;

													xmlMethodInvocation.setAttribute("Arguments", methodInvocation.arguments().toString());
													xmlMethodInvocation.setAttribute("InvokedBy", methodDeclaration.getName().getFullyQualifiedName());
													
												}
												
																			
												Element xmlMethodAssignment = new Element("MethodAssignments");
												xmlMethod.addContent(xmlMethodAssignment);
												
												AssignmentVisitor methodAssignmentVisitor = new AssignmentVisitor();
												methodDeclaration.accept(methodAssignmentVisitor);
												for (Assignment methodAssignment : methodAssignmentVisitor.getAssignments()) {
													Element xmlMethodAssignmentExpression= new Element("Assignment");
													xmlMethodAssignment.addContent(xmlMethodAssignmentExpression);
													xmlMethodAssignmentExpression.setAttribute("LeftHandSide", methodAssignment.getLeftHandSide().toString());
													xmlMethodAssignmentExpression.setAttribute("RightHandSide", methodAssignment.getRightHandSide().toString());
												}

												Element xmlMethodExceptions = new Element("MethodExceptions");
												xmlMethod.addContent(xmlMethodExceptions);
												for (ITypeBinding exceptions : methodDeclaration.resolveBinding().getExceptionTypes()) {
													Element xmlMethodException = new Element("Exception");
													xmlMethodExceptions.addContent(xmlMethodException);
													xmlMethodException.setAttribute("ExceptionType", exceptions.getName());	
												}
							
											}
										}	
								}
						
							}
						}

						
						
						lineStr=String.valueOf(LinesOfCode);
						packageStr= String.valueOf(packageCounter);
						classStr=String.valueOf(classCounter);
						attributeStr=String.valueOf(attributeCounter);
						methodStr=String.valueOf(methodCounter);
						commentStr=String.valueOf(commentCounter);
						localVarStr=String.valueOf(localVarCounter);
						
						inheritanceStr=String.valueOf(inheritanceCounter);
						MethodInvocationStr=String.valueOf(MethodInvocationCounter);
						AttributeAccessStr=String.valueOf(AttributeAccessCounter);
					    
						xmlMetricLOC.setAttribute("LOC", lineStr);
						xmlMetricNOP.setAttribute("NOP", packageStr);
						xmlMetricNOC.setAttribute("NOC", classStr);
						xmlMetricNOA.setAttribute("NOA", attributeStr);
						xmlMetricNOM.setAttribute("NOM", methodStr);
						xmlMetricNOCo.setAttribute("NOCo", commentStr);
						xmlMetricNOLv.setAttribute("NOLv", localVarStr);
						
						xmlMetricNOIn.setAttribute("NOIn", inheritanceStr);
						xmlMetricNOI.setAttribute("NOI", MethodInvocationStr);
						xmlMetricNOAc.setAttribute("NOAc", AttributeAccessStr);
						
						Bundle bundle = FrameworkUtil.getBundle(SampleHandler.class);
						File pluginFolder = FileLocator.toFileURL(bundle.getEntry("/")).getPath() != null
						        ? new File(FileLocator.toFileURL(bundle.getEntry("/")).toURI())
						        : null;

						File xmlFolder = new File(pluginFolder, "XML");
						if (!xmlFolder.exists())
						    xmlFolder.mkdirs();

						File xmlFile = new File(xmlFolder, project.getName() + ".xml");
						File metricsFile = new File(xmlFolder, project.getName() + " - codeMetrics.xml");

						new XMLOutputter(Format.getPrettyFormat())
						        .output(doc, new FileOutputStream(xmlFile));

						new XMLOutputter(Format.getPrettyFormat())
						        .output(codeMetricsDoc, new FileOutputStream(metricsFile));
					}
				
					
				} 
				
				
				catch (FileNotFoundException e) {
				e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JavaModelException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				}
//				catch (NullPointerException e) {
//					e.printStackTrace();
//				}
 catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
//			return null;
					
		    System.out.println("==[The execution time in ms]=========================================");
			long time2 = System.currentTimeMillis();
			System.out.println("The execution time in ms is equal to: "+(time2-time1));
			System.out.println("=====================================================================");

			MessageDialog.openInformation(
				    HandlerUtil.getActiveShell(event),
				    "Code Parser",
				    "All done! XML files have been created successfully."
				);
			
			return null;
		}


		// read file content into a string
		public static String readFileToString(String filePath) throws IOException {
			StringBuilder fileData = new StringBuilder(1000);
			BufferedReader reader = new BufferedReader(new FileReader(filePath));

			char[] buf = new char[10];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}

			reader.close();
			return fileData.toString();
		}

		public static void deleteAllDocuments() throws IOException, URISyntaxException {

		    Bundle bundle = FrameworkUtil.getBundle(SampleHandler.class);

		    File pluginFolder = new File(
		        FileLocator.toFileURL(bundle.getEntry("/")).toURI()
		    );

		    File xmlFolder = new File(pluginFolder, "XML");

		    if (!xmlFolder.exists()) {
		        xmlFolder.mkdirs();
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
		
