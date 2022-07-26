package com.black.router.process;

import com.black.router.annotation.Route;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.black.router.annotation.Route")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RouteProcessor extends AbstractProcessor {
    private final String KEY_MODULE_NAME = "moduleName";

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String moduleName = "";

        Map<String, String> options = processingEnv.getOptions();
        if (isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        System.out.println("moduleName = " + moduleName);

        StringBuilder importSb = new StringBuilder();
        importSb.append("import com.black.router.annotation.RouteConfigHelper; \n");
        importSb.append("import java.lang.String; \n");
        importSb.append("import java.util.Map; \n");
        importSb.append("import java.lang.Class; \n");
        importSb.append("import com.black.router.annotation.Config; \n");
        StringBuilder putSb = new StringBuilder();
        // for each javax.lang.model.element.Element annotated with the CustomAnnotation
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Route.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                String objectFullType = element.asType().toString();
                String objectType = element.getSimpleName().toString();
                Route route = element.getAnnotation(Route.class);
                String[] output = route.value();
                importSb.append("import ").append(objectFullType).append(";\n");
                String configBuilder = " new Config(" + objectType + ".class, \"" + route.beforePath() + "\", \"" + route.fragmentParentPath() + "\", " + route.fragmentIndex() + ")";
                for (String value : output) {
                    putSb.append("\t\tmap.put(\"").append(value).append("\", ").append(configBuilder).append(");\n");
                }
            }
        }

        String className = "Module$$" + moduleName + "$$GeneratedClass";

        StringBuilder builder = new StringBuilder();
        builder.append("package com.black.router.generated;\n\n");
        builder.append(importSb);
        builder.append("\n\n");
        builder.append("public class " + className + "  implements RouteConfigHelper {\n\n"); // open class
        builder.append("\t@Override\n"); // open method
        builder.append("\tpublic void initRouteConfig(Map<String, Config> map) {\n"); // open method
        builder.append(putSb);
        builder.append("\t}\n");
        builder.append("}");

        try { // write the file
            JavaFileObject source = processingEnv.getFiler().createSourceFile("com.black.router.generated." + className);
            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
//        StringBuilder configStringBuilder = new StringBuilder();
//        configStringBuilder.append("package com.black.route.generated;\n\n");
//        configStringBuilder.append("import java.lang.String; \n");
//        configStringBuilder.append("import java.util.Map; \n");
//        configStringBuilder.append("import java.lang.Class; \n");
//        configStringBuilder.append("\n\n");
//        configStringBuilder.append("public class Config {\n\n");
//        configStringBuilder.append("\tpublic Class clz;\n");
//        configStringBuilder.append("\tpublic String beforePath;\n");
//        configStringBuilder.append("\tpublic String fragmentParentPath;\n\n");
//        configStringBuilder.append("\tpublic Config(Class clz, String beforePath, String fragmentParentPath) {n");
//        configStringBuilder.append("\t\tthis.clz = clz;n");
//        configStringBuilder.append("\t\tthis.beforePath = beforePath;n");
//        configStringBuilder.append("\t\tthis.fragmentParentPath = fragmentParentPath;n");
//        configStringBuilder.append("\t}n");
//        configStringBuilder.append("}");
//        try { // write the file
//            JavaFileObject source = processingEnv.getFiler().createSourceFile("com.black.route.generated.Config");
//            Writer writer = source.openWriter();
//            writer.write(configStringBuilder.toString());
//            writer.flush();
//            writer.close();
//        } catch (IOException ignored) {
//        }
        return true;
    }

    private boolean isNotEmpty(Map<String, String> options) {
        return options != null && !options.isEmpty();
    }
}
