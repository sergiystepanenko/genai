package com.epam.genai.service;

import com.epam.genai.service.semantickernal.model.BookFormat;
import com.epam.genai.service.semantickernal.model.BookStore;
import java.util.EnumMap;
import org.springframework.stereotype.Service;

@Service
public class BookStoreService {
  private static final EnumMap<BookFormat, BookStore> bookStoreMap = new EnumMap<>(BookFormat.class);

  static {
    bookStoreMap.put(BookFormat.HARDCOVER, new BookStore("Amazon", "https://www.amazon.com"));
    bookStoreMap.put(BookFormat.PAPERBACK, new BookStore("Barnes & Noble", "https://www.barnesandnoble.com"));
    bookStoreMap.put(BookFormat.MASS_MARKET_PAPERBACK, new BookStore("ThriftBooks", "https://www.thriftbooks.com"));
    bookStoreMap.put(BookFormat.EBOOK, new BookStore("Kindle Store", "https://www.amazon.com/Kindle-eBooks"));
    bookStoreMap.put(BookFormat.SUBSCRIPTION_SERVICE, new BookStore("Scribd", "https://www.scribd.com"));
    bookStoreMap.put(BookFormat.AUDIOBOOK, new BookStore("Audible", "https://www.audible.com"));
  }

  public BookStore getBookStore(BookFormat bookFormat) {
    return bookStoreMap.get(bookFormat);
  }
}
