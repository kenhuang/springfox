package com.mangofactory.documentation.swagger.web

import com.mangofactory.documentation.service.PathProvider
import com.mangofactory.documentation.spring.web.mixins.RequestMappingSupport
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.ServletContext

@Mixin(RequestMappingSupport)
class AbsolutePathProviderSpec extends Specification {

   def "assert urls"() {
      given:
        AbsolutePathProvider provider = new AbsolutePathProvider(servletContext: servletContext)

      expect:
        provider.applicationPath() == expectedAppPath
        provider.getDocumentationPath() == expectedDocPath

      where:
        servletContext   | expectedAppPath                      | expectedDocPath
        servletContext() | "http://localhost:8080/context-path" | "http://localhost:8080/context-path/api-docs"
        mockContext("")  | "http://localhost:8080"              | "http://localhost:8080/api-docs"

   }

  @Unroll
  def "Absolute paths"() {
    given:
      PathProvider provider = new AbsolutePathProvider(apiResourcePrefix: "", servletContext: servletContext())

    expect:
      provider.getApplicationBasePath() == expectedAppBase
      provider.getResourceListingPath(groupName, apiDeclaration) == expectedDoc

    where:
      groupName    | apiDeclaration     | expectedAppBase                      | expectedDoc
      'default'       | 'api-declaration'  | "http://localhost:8080/context-path" | "http://localhost:8080/context-path/api-docs/default/api-declaration"
      'somethingElse' | 'api-declaration2' | "http://localhost:8080/context-path" | "http://localhost:8080/context-path/api-docs/somethingElse/api-declaration2"

  }

   private mockContext(String path) {
      [getContextPath: { return path }] as ServletContext
   }
}