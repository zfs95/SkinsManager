package com.example.SkinsManager.service

import com.example.SkinsManager.model.Product
import com.example.SkinsManager.repository.ProductRepository
import jakarta.annotation.PostConstruct
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.*
import org.apache.lucene.index.*
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.store.RAMDirectory
import org.springframework.stereotype.Service

@Service
class ProductSearchService(
    private val productRepository: ProductRepository
) {

    private val analyzer = StandardAnalyzer()
    private val index = RAMDirectory()
    private lateinit var writer: IndexWriter

    @PostConstruct
    fun init() {
        val config = IndexWriterConfig(analyzer)
        writer = IndexWriter(index, config)
        rebuildIndex()
    }

    fun rebuildIndex() {
        writer.deleteAll()
        val products = productRepository.findAll()
        products.forEach { addProductToIndex(it) }
        writer.commit()
    }

    private fun addProductToIndex(product: Product) {
        val doc = Document()
        doc.add(StringField("id", product.id.toString(), Field.Store.YES))
        doc.add(TextField("marketHashName", product.marketHashName, Field.Store.YES))
        writer.addDocument(doc)
    }

    fun addOrUpdateProduct(product: Product) {
        val doc = Document()
        doc.add(StringField("id", product.id.toString(), Field.Store.YES))
        doc.add(TextField("marketHashName", product.marketHashName, Field.Store.YES))
        writer.updateDocument(Term("id", product.id.toString()), doc)
        writer.commit()
    }

    fun search(queryStr: String, maxResults: Int = 20): List<Long> {
        DirectoryReader.open(writer).use { reader ->
            val searcher = IndexSearcher(reader)
            val parser = QueryParser("marketHashName", analyzer)
            parser.allowLeadingWildcard = true
            val query: Query = parser.parse("*$queryStr*")
            val hits: Array<ScoreDoc> = searcher.search(query, maxResults).scoreDocs
            return hits.map { hit ->
                val doc = searcher.doc(hit.doc)
                doc.get("id")!!.toLong()
            }
        }
    }
}