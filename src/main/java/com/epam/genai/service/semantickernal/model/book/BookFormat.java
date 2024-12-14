package com.epam.genai.service.semantickernal.model.book;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookFormat {
  // Physical Book Formats
  HARDCOVER("Hardcover", "Durable book with a rigid protective cover, long-lasting but more expensive.", true),
  PAPERBACK("Paperback", "Flexible, softer cover, lighter, and more affordable.", true),
  MASS_MARKET_PAPERBACK("Mass market paperback", "Smaller, portable, and least expensive physical option.", true),

  // Digital Formats
  EBOOK("E-book", "Digital books in formats like PDF, EPUB, or Kindle, readable on devices.", false),
  SUBSCRIPTION_SERVICE("Subscription service", "Access to digital books through platforms with a subscription fee.", false),

  // Audiobooks
  AUDIOBOOK("Audio book", "Books in audio format, available for download or streaming, ideal for on-the-go.", false);

  private final String name;
  private final String description;
  private final boolean isPhysical;
}
