package springdox.documentation.spring.web.plugins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import springdox.documentation.service.ApiListing;
import springdox.documentation.service.Operation;
import springdox.documentation.service.Parameter;
import springdox.documentation.spi.DocumentationType;
import springdox.documentation.spi.schema.contexts.ModelContext;
import springdox.documentation.spi.service.ApiListingBuilderPlugin;
import springdox.documentation.spi.service.DefaultsProviderPlugin;
import springdox.documentation.spi.service.DocumentationPlugin;
import springdox.documentation.spi.service.ExpandedParameterBuilderPlugin;
import springdox.documentation.spi.service.OperationBuilderPlugin;
import springdox.documentation.spi.service.OperationModelsProviderPlugin;
import springdox.documentation.spi.service.ParameterBuilderPlugin;
import springdox.documentation.spi.service.ResourceGroupingStrategy;
import springdox.documentation.spi.service.contexts.ApiListingContext;
import springdox.documentation.spi.service.contexts.DocumentationContextBuilder;
import springdox.documentation.spi.service.contexts.OperationContext;
import springdox.documentation.spi.service.contexts.ParameterContext;
import springdox.documentation.spi.service.contexts.ParameterExpansionContext;
import springdox.documentation.spi.service.contexts.RequestMappingContext;
import springdox.documentation.spring.web.SpringGroupingStrategy;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.*;

@Component
public class DocumentationPluginsManager {
  private final PluginRegistry<DocumentationPlugin, DocumentationType> documentationPlugins;
  private final PluginRegistry<ApiListingBuilderPlugin, DocumentationType> apiListingPlugins;
  private final PluginRegistry<ParameterBuilderPlugin, DocumentationType> parameterPlugins;
  private final PluginRegistry<ExpandedParameterBuilderPlugin, DocumentationType> parameterExpanderPlugins;
  private final PluginRegistry<OperationBuilderPlugin, DocumentationType> operationBuilderPlugins;
  private final PluginRegistry<ResourceGroupingStrategy, DocumentationType> resourceGroupingStrategies;
  private final PluginRegistry<OperationModelsProviderPlugin, DocumentationType> operationModelsProviders;
  private final PluginRegistry<DefaultsProviderPlugin, DocumentationType> defaultsProviders;

  @Autowired
  public DocumentationPluginsManager(
          @Qualifier("documentationPluginRegistry")
          PluginRegistry<DocumentationPlugin, DocumentationType> documentationPlugins,
          @Qualifier("apiListingBuilderPluginRegistry")
          PluginRegistry<ApiListingBuilderPlugin, DocumentationType> apiListingPlugins,
          @Qualifier("parameterBuilderPluginRegistry")
          PluginRegistry<ParameterBuilderPlugin, DocumentationType> parameterPlugins,
          @Qualifier("expandedParameterBuilderPluginRegistry")
          PluginRegistry<ExpandedParameterBuilderPlugin, DocumentationType> parameterExpanderPlugins,
          @Qualifier("operationBuilderPluginRegistry")
          PluginRegistry<OperationBuilderPlugin, DocumentationType> operationBuilderPlugins,
          @Qualifier("resourceGroupingStrategyRegistry")
          PluginRegistry<ResourceGroupingStrategy, DocumentationType> resourceGroupingStrategies,
          @Qualifier("operationModelsProviderPluginRegistry")
          PluginRegistry<OperationModelsProviderPlugin, DocumentationType> operationModelsProviders,
          @Qualifier("defaultsProviderPluginRegistry")
          PluginRegistry<DefaultsProviderPlugin, DocumentationType> defaultsProviders) {
    this.documentationPlugins = documentationPlugins;
    this.apiListingPlugins = apiListingPlugins;
    this.parameterPlugins = parameterPlugins;
    this.parameterExpanderPlugins = parameterExpanderPlugins;
    this.operationBuilderPlugins = operationBuilderPlugins;
    this.resourceGroupingStrategies = resourceGroupingStrategies;
    this.operationModelsProviders = operationModelsProviders;
    this.defaultsProviders = defaultsProviders;
  }


  public List<DocumentationPlugin> documentationPlugins() {
    List<DocumentationPlugin> plugins = documentationPlugins.getPlugins();
    if (plugins.isEmpty()) {
      return newArrayList(defaultDocumentationPlugin());
    }
    return plugins;
  }

  public Parameter parameter(ParameterContext parameterContext) {
    for (ParameterBuilderPlugin each : parameterPlugins.getPluginsFor(parameterContext.getDocumentationType())) {
      each.apply(parameterContext);
    }
    return parameterContext.parameterBuilder().build();
  }

  public Parameter expandParameter(ParameterExpansionContext context) {
    for (ExpandedParameterBuilderPlugin each : parameterExpanderPlugins.getPluginsFor(context.getDocumentationType())) {
      each.apply(context);
    }
    return context.getParameterBuilder().build();
  }

  public Operation operation(OperationContext operationContext) {
    for (OperationBuilderPlugin each : operationBuilderPlugins.getPluginsFor(operationContext.getDocumentationType())) {
      each.apply(operationContext);
    }
    return operationContext.operationBuilder().build();
  }

  public ApiListing apiListing(ApiListingContext context) {
    for (ApiListingBuilderPlugin each : apiListingPlugins.getPluginsFor(context.getDocumentationType())) {
      each.apply(context);
    }
    return context.apiListingBuilder().build();
  }

  public Set<ModelContext> modelContexts(RequestMappingContext context) {
    DocumentationType documentationType = context.getDocumentationContext().getDocumentationType();
    for (OperationModelsProviderPlugin each : operationModelsProviders.getPluginsFor(documentationType)) {
      each.apply(context);
    }
    return context.operationModelsBuilder().build();
  }

  public ResourceGroupingStrategy resourceGroupingStrategy(DocumentationType documentationType) {
    return resourceGroupingStrategies.getPluginFor(documentationType, new SpringGroupingStrategy());
  }

  private DocumentationPlugin defaultDocumentationPlugin() {
    return new DocumentationConfigurer(DocumentationType.SWAGGER_12);
  }

  public DocumentationContextBuilder createContextBuilder(DocumentationType documentationType,
          DefaultConfiguration defaultConfiguration) {
    return defaultsProviders.getPluginFor(documentationType, defaultConfiguration)
            .create(documentationType)
            .withResourceGroupingStrategy(resourceGroupingStrategy(documentationType));
  }
}