package com.epam.genai.service.semantickernal;

import com.epam.genai.service.BookStoreService;
import com.epam.genai.service.semantickernal.model.book.BookFormat;
import com.epam.genai.service.semantickernal.model.book.BookStore;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookPlugin {

  private final BookStoreService bookStoreService;

  @DefineKernelFunction(
      name = "where_to_buy",
      description = "Propose a place where and what format to buy the book.",
      returnDescription = "Returns the book store name and instructions how to access it.",
      returnType = "com.epam.genai.service.semantickernal.model.book.BookStore")
  public Mono<BookStore> whereToBuyBook(
      @KernelFunctionParameter(name = "bookFormat", description = "The format of the book", type = BookFormat.class)
      BookFormat bookFormat,
      @KernelFunctionParameter(name = "minPrice", description = "The minimum price of the book a user ready to pay", type = Double.class)
      Double minPrice,
      @KernelFunctionParameter(name = "maxPrice", description = "The maximum price of the book a user ready to pay", type = Double.class)
      Double maxPrice
  ) {
    log.debug("Function where_to_buy: bookFormat={}, minPrice={}, maxPrice={}", bookFormat, minPrice, maxPrice);

    if (bookFormat == null) {
      log.warn("Function where_to_buy: bookFormat is null");
      return Mono.just(bookStoreService.getBookStore(BookFormat.HARDCOVER));
    }

    BookStore bookStore = bookStoreService.getBookStore(bookFormat);
    return Mono.just(bookStore);
  }
}
