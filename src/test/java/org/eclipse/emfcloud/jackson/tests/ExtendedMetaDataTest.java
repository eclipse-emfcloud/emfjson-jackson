package org.eclipse.emfcloud.jackson.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.emfcloud.jackson.junit.model.Author;
import org.eclipse.emfcloud.jackson.junit.model.Book;
import org.eclipse.emfcloud.jackson.junit.model.ModelFactory;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExtendedMetaDataTest {

   @Test
   public void testNoExtendedMetaDataNames() {
      final EMFModule emfModule = new EMFModule();
      emfModule.configure(EMFModule.Feature.OPTION_USE_NAMES_FROM_EXTENDED_META_DATA, false);
      final ObjectMapper mapper = new ObjectMapper().registerModule(emfModule);

      final Book book = ModelFactory.eINSTANCE.createBook();
      book.setAuthorName("Friedrich Schiller");

      final Author author = ModelFactory.eINSTANCE.createAuthor();
      author.setFirstName("Friedrich");
      author.setLastName("Schiller");
      book.setAuthor(author);

      final JsonNode json = mapper.valueToTree(book);
      assertEquals("Friedrich Schiller", json.get("authorName").asText());
      assertEquals("Friedrich", json.get("author").get("firstName").asText());
      assertEquals("Schiller", json.get("author").get("lastName").asText());
   }

   @Test
   public void testDefaultExtendedMetaDataNames() {
      final ObjectMapper mapper = EMFModule.setupDefaultMapper();

      final Book book = ModelFactory.eINSTANCE.createBook();
      book.setAuthorName("Friedrich Schiller");

      final JsonNode json = mapper.valueToTree(book);
      assertEquals("Friedrich Schiller", json.get("author").asText());
   }

   @Test
   public void testExtendedMetaDataNames() {
      final EMFModule emfModule = new EMFModule();
      emfModule.configure(EMFModule.Feature.OPTION_USE_NAMES_FROM_EXTENDED_META_DATA, true);
      final ObjectMapper mapper = new ObjectMapper().registerModule(emfModule);

      final Book book = ModelFactory.eINSTANCE.createBook();
      book.setAuthorName("Friedrich Schiller");

      final JsonNode json = mapper.valueToTree(book);
      assertEquals("Friedrich Schiller", json.get("author").asText());
   }
}
