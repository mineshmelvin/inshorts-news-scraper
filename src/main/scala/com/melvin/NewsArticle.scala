package com.melvin
case class NewsArticle(
                         id: String,
                         date: String,
                         time: String,
                         category: String,
                         title: String,
                         content: String,
                         imageUrl: String,
                         readMoreUrl: String,
                         url: String,
                         author: String
                      )
case class NewsResponse(
                         category: String,
                         data: List[NewsArticle],
                         success: String
                       )
