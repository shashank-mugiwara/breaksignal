---
title: Building RAG Applications with RedisSearch and Ollama - A Complete Local Setup Guide
description: Learn how to build production-ready Retrieval-Augmented Generation (RAG) applications using RedisSearch for vector storage, Ollama for local LLM hosting, and understand embeddings, vector similarity, and cosine similarity from first principles.
published: true
date: 2026-01-01T00:00:00.000Z
tags: rag, redissearch, redis, ollama, embeddings, vector similarity, cosine similarity, mistral, llm, ai, machine learning, vector database, semantic search
editor: markdown
dateCreated: 2026-01-01T00:00:00.000Z
---

# Building RAG Applications with RedisSearch and Ollama - A Complete Local Setup Guide

## Introduction

In the era of large language models, one of the most significant challenges is getting LLMs to work with your proprietary data without hallucinating or providing outdated information. While models like GPT-4, Claude, or Mistral are incredibly powerful, they're limited by their training cutoff dates and don't have access to your private documents, databases, or real-time information.

This is where Retrieval-Augmented Generation (RAG) comes into play. RAG combines the generative capabilities of LLMs with the precision of information retrieval systems. Instead of fine-tuning a model on your data (which is expensive and time-consuming), RAG retrieves relevant context from your documents and feeds it to the LLM, allowing it to generate accurate, contextually relevant responses.

In this comprehensive guide, we'll build a complete RAG system from scratch using RedisSearch as our vector database and Ollama for running models locally. We'll dive deep into the fundamental concepts of embeddings, vector similarity, and why cosine similarity has become the de facto standard for semantic search. By the end, you'll have a working RAG application running entirely on your local machine.

## Understanding the RAG Architecture

Before we dive into implementation, let's understand how RAG works. The architecture consists of two main phases:

### Phase 1: Indexing (Offline)
1. Split your documents into smaller chunks
2. Generate embeddings (vector representations) for each chunk using an embedding model
3. Store these embeddings in a vector database (RedisSearch in our case)

### Phase 2: Retrieval and Generation (Runtime)
1. User asks a question
2. Generate an embedding for the question
3. Search the vector database for the most similar document chunks
4. Feed the retrieved chunks along with the question to an LLM
5. LLM generates a response based on the retrieved context

```
User Query: "How do I configure authentication?"
     |
     v
[Embedding Model] --> Query Vector: [0.23, -0.45, 0.12, ...]
     |
     v
[Vector Database Search] --> Most Similar Chunks:
     |                        - "Authentication is configured in config.yml..."
     |                        - "To enable OAuth, set oauth.enabled=true..."
     |                        - "JWT tokens expire after 24 hours..."
     v
[LLM with Context] --> "Based on your documentation, authentication
                        is configured in the config.yml file. You need
                        to set the following parameters..."
```

## What Are Embeddings?

Embeddings are one of the most fundamental concepts in modern AI, yet they're often misunderstood. At their core, embeddings are numerical representations of data—a way to convert text, images, or any other type of information into vectors of numbers that machines can understand and compare.

### The Motivation Behind Embeddings

Computers don't understand words the way humans do. When you see the word "dog," your brain immediately conjures up images, associations, and meanings. A computer just sees a sequence of characters: 'd', 'o', 'g'. Embeddings bridge this gap by converting semantic meaning into mathematical representations.

Consider these sentences:
- "The dog barked loudly"
- "The canine made a loud sound"
- "The cat meowed quietly"

To a traditional keyword search, the first two sentences appear completely different (no shared words except "the"). But semantically, they mean almost the same thing. The third sentence shares more words with the first ("the" and "loudly"/"quietly" are similar), but the meaning is quite different.

Embeddings solve this problem by representing text in a high-dimensional space where semantically similar content is positioned closer together. Our two dog sentences would have vectors that are very close to each other, while the cat sentence would be further away.

### How Embeddings Work

Modern embedding models like `nomic-embed-text` are neural networks trained on massive amounts of text data. They learn to map words, sentences, or paragraphs to vectors (arrays of floating-point numbers) such that:

1. **Semantic similarity is preserved**: Similar meanings result in similar vectors
2. **Context is captured**: The same word in different contexts gets different representations
3. **Relationships are encoded**: Analogies and relationships are preserved in vector space

For example, a sentence might be converted to a 768-dimensional vector:
```
"Redis is a fast in-memory database" → [0.23, -0.45, 0.12, 0.89, ..., -0.34]
                                        (768 numbers total)
```

Each dimension captures some aspect of meaning—though not in a way that's directly interpretable by humans. Some dimensions might capture whether text is technical, some might capture sentiment, others might encode domain-specific concepts.

### Why High Dimensions?

You might wonder why we need 768 or even 1536 dimensions. The answer lies in the complexity of human language. Language is incredibly nuanced—the same sentence can mean different things based on context, tone, domain, and cultural background. High-dimensional spaces give the model enough "room" to encode all these subtleties without different concepts colliding.

Think of it this way: if you tried to map all the world's cities using just latitude (1 dimension), many cities would overlap. Add longitude (2 dimensions), and you can uniquely identify most cities. Add elevation (3 dimensions), and you can distinguish cities at different altitudes. Natural language is far more complex than geography, hence the need for hundreds or thousands of dimensions.

## Vector Similarity: Measuring Semantic Closeness

Once we have embeddings, we need a way to measure how similar two vectors are. This is where vector similarity metrics come into play. There are several ways to measure the "distance" or "similarity" between vectors:

### 1. Euclidean Distance (L2 Distance)

Euclidean distance is the "straight-line" distance between two points in space. It's calculated using the Pythagorean theorem:

```
distance = √[(x₁-x₂)² + (y₁-y₂)² + (z₁-z₂)² + ...]
```

For vectors A = [1, 2, 3] and B = [4, 5, 6]:
```
distance = √[(1-4)² + (2-5)² + (3-6)²]
         = √[9 + 9 + 9]
         = √27 ≈ 5.20
```

**Pros**: Intuitive, considers magnitude
**Cons**: Sensitive to vector length, computationally expensive for high dimensions

### 2. Manhattan Distance (L1 Distance)

Manhattan distance (also called taxicab distance) measures the distance if you could only travel along axes (like navigating city blocks):

```
distance = |x₁-x₂| + |y₁-y₂| + |z₁-z₂| + ...
```

For the same vectors A = [1, 2, 3] and B = [4, 5, 6]:
```
distance = |1-4| + |2-5| + |3-6| = 3 + 3 + 3 = 9
```

**Pros**: Computationally faster than Euclidean
**Cons**: Not suitable for embeddings, ignores diagonal relationships

### 3. Dot Product

The dot product measures how much two vectors point in the same direction:

```
dot_product = (x₁ × x₂) + (y₁ × y₂) + (z₁ × z₂) + ...
```

For vectors A = [1, 2, 3] and B = [4, 5, 6]:
```
dot_product = (1×4) + (2×5) + (3×6) = 4 + 10 + 18 = 32
```

**Pros**: Fast to compute
**Cons**: Affected by vector magnitude, not normalized

### 4. Cosine Similarity

Cosine similarity measures the cosine of the angle between two vectors, effectively measuring orientation regardless of magnitude:

```
cosine_similarity = (A · B) / (||A|| × ||B||)
```

Where:
- `A · B` is the dot product
- `||A||` and `||B||` are the magnitudes (lengths) of the vectors

For vectors A = [1, 2, 3] and B = [4, 5, 6]:
```
A · B = 32 (calculated above)
||A|| = √(1² + 2² + 3²) = √14 ≈ 3.74
||B|| = √(4² + 5² + 6²) = √77 ≈ 8.77

cosine_similarity = 32 / (3.74 × 8.77) ≈ 0.975
```

The result ranges from -1 (opposite directions) to 1 (same direction), with 0 meaning perpendicular.

**Pros**: Normalized, not affected by magnitude, perfect for embeddings
**Cons**: Slightly more expensive to compute than dot product

## Why Cosine Similarity for Embeddings?

Cosine similarity has become the standard for comparing embeddings, and there are compelling reasons for this:

### 1. Magnitude Independence

When comparing text embeddings, we care about semantic similarity, not the length of the text. Consider these examples:

- "dog" → vector A (short text)
- "The dog is a domesticated carnivorous mammal" → vector B (long text)

Both are about dogs, but vector B might have a larger magnitude simply because it represents more text. Euclidean distance would show these as far apart, but cosine similarity correctly identifies them as semantically similar because it only considers the direction (orientation) of the vectors, not their length.

### 2. Normalized Comparison

Most embedding models normalize their output vectors to unit length (magnitude of 1). This makes cosine similarity equivalent to the dot product but with the semantic guarantee that we're measuring pure similarity, not scale. It creates a level playing field where "cat" and "The feline species known as Felis catus" can be fairly compared.

### 3. Interpretable Scores

Cosine similarity produces scores between -1 and 1, which are intuitive:
- 1.0 = identical semantic meaning
- 0.0 = completely unrelated
- -1.0 = opposite meaning

This makes it easy to set thresholds for filtering results.

### 4. Works Well in High Dimensions

In high-dimensional spaces (like 768 dimensions), Euclidean distance suffers from the "curse of dimensionality"—all points tend to become roughly equidistant from each other. Cosine similarity doesn't have this problem because it measures angles, which remain discriminative even in high dimensions.

### 5. Empirically Proven

Extensive research and real-world applications have shown that cosine similarity consistently outperforms other metrics for semantic search tasks. It's not just theoretical—it works better in practice.

## Setting Up Your Local Environment

Now that we understand the theory, let's build a working RAG system. We'll set up everything to run locally, giving you complete control and privacy over your data.

### Prerequisites

- macOS, Linux, or Windows with WSL2
- At least 8GB RAM (16GB recommended for larger models)
- 10GB free disk space
- Python 3.8 or higher

### Step 1: Installing Redis Stack

Redis Stack includes RedisSearch, which provides vector similarity search capabilities on top of Redis. It's the easiest way to get a production-ready vector database running locally.

**On macOS (using Homebrew):**
```bash
brew tap redis-stack/redis-stack
brew install redis-stack
```

**On Linux:**
```bash
curl -fsSL https://packages.redis.io/gpg | sudo gpg --dearmor -o /usr/share/keyrings/redis-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/redis-archive-keyring.gpg] https://packages.redis.io/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/redis.list
sudo apt-get update
sudo apt-get install redis-stack-server
```

**Start Redis Stack:**
```bash
redis-stack-server
```

You should see output indicating Redis is running on port 6379. Keep this terminal window open.

**Verify Installation:**
Open a new terminal and run:
```bash
redis-cli ping
```

You should see `PONG` in response.

### Step 2: Installing Ollama

Ollama makes it incredibly easy to run large language models locally. It handles model downloading, GPU acceleration, and provides a simple API.

**On macOS and Linux:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

**On Windows:**
Download the installer from https://ollama.com/download

**Start Ollama:**
```bash
ollama serve
```

This starts the Ollama server in the background.

**Pull the Models:**

We'll use Mistral for text generation and nomic-embed-text for creating embeddings:

```bash
# Pull Mistral (7B parameter model, ~4GB)
ollama pull mistral

# Pull nomic-embed-text (embedding model, ~274MB)
ollama pull nomic-embed-text
```

The first pull might take some time depending on your internet connection.

**Test the Models:**
```bash
# Test Mistral
ollama run mistral "What is Redis?"

# Test embeddings (you should see a vector output)
curl http://localhost:11434/api/embeddings -d '{
  "model": "nomic-embed-text",
  "prompt": "The quick brown fox"
}'
```

### Step 3: Installing Python Dependencies

Create a new project directory and set up a virtual environment:

```bash
mkdir rag-redissearch
cd rag-redissearch
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

pip install redis ollama numpy
```

## Building the RAG Application

Now let's build our RAG system step by step. We'll create a complete application that can index documents and answer questions based on them.

### Creating the Document Indexer

First, let's create a script that chunks documents, generates embeddings, and stores them in RedisSearch:

```python
import redis
from redis.commands.search.field import TextField, VectorField, NumericField
from redis.commands.search.indexDefinition import IndexDefinition, IndexType
from redis.commands.search.query import Query
import ollama
import numpy as np
import hashlib
from typing import List, Dict

class DocumentIndexer:
    def __init__(self, redis_host='localhost', redis_port=6379):
        """Initialize Redis connection and create vector index."""
        self.redis_client = redis.Redis(
            host=redis_host,
            port=redis_port,
            decode_responses=True
        )
        self.index_name = "documents_idx"
        self.vector_dim = 768  # nomic-embed-text produces 768-dim vectors

        # Create the index if it doesn't exist
        self._create_index()

    def _create_index(self):
        """Create a RediSearch index with vector similarity support."""
        try:
            # Try to get info about existing index
            self.redis_client.ft(self.index_name).info()
            print(f"Index '{self.index_name}' already exists")
        except redis.ResponseError:
            # Index doesn't exist, create it
            schema = (
                TextField("text"),
                TextField("source"),
                NumericField("chunk_id"),
                VectorField(
                    "embedding",
                    "FLAT",  # Can also use "HNSW" for larger datasets
                    {
                        "TYPE": "FLOAT32",
                        "DIM": self.vector_dim,
                        "DISTANCE_METRIC": "COSINE",  # Using cosine similarity
                    }
                ),
            )

            definition = IndexDefinition(
                prefix=["doc:"],
                index_type=IndexType.HASH
            )

            self.redis_client.ft(self.index_name).create_index(
                fields=schema,
                definition=definition
            )
            print(f"Created index '{self.index_name}'")

    def chunk_text(self, text: str, chunk_size: int = 500, overlap: int = 50) -> List[str]:
        """
        Split text into overlapping chunks.

        Overlap is important because it ensures that context isn't lost
        at chunk boundaries. If a key concept appears at the end of one
        chunk, it will also appear at the start of the next.
        """
        words = text.split()
        chunks = []

        for i in range(0, len(words), chunk_size - overlap):
            chunk = ' '.join(words[i:i + chunk_size])
            if chunk:  # Don't add empty chunks
                chunks.append(chunk)

            # Break if this is the last chunk
            if i + chunk_size >= len(words):
                break

        return chunks

    def generate_embedding(self, text: str) -> List[float]:
        """Generate embedding vector using Ollama's nomic-embed-text model."""
        response = ollama.embeddings(
            model='nomic-embed-text',
            prompt=text
        )
        return response['embedding']

    def index_document(self, text: str, source: str):
        """
        Index a document by chunking it and storing embeddings in Redis.

        Args:
            text: The document text to index
            source: Source identifier (filename, URL, etc.)
        """
        chunks = self.chunk_text(text)
        print(f"Split document into {len(chunks)} chunks")

        for idx, chunk in enumerate(chunks):
            # Generate unique ID for this chunk
            chunk_hash = hashlib.md5(f"{source}:{idx}".encode()).hexdigest()
            doc_key = f"doc:{chunk_hash}"

            # Generate embedding
            embedding = self.generate_embedding(chunk)

            # Convert embedding to bytes for Redis
            embedding_bytes = np.array(embedding, dtype=np.float32).tobytes()

            # Store in Redis
            self.redis_client.hset(
                doc_key,
                mapping={
                    "text": chunk,
                    "source": source,
                    "chunk_id": idx,
                    "embedding": embedding_bytes
                }
            )

            print(f"Indexed chunk {idx + 1}/{len(chunks)} from {source}")

        print(f"Successfully indexed document: {source}")

    def search(self, query: str, top_k: int = 3) -> List[Dict]:
        """
        Search for similar documents using vector similarity.

        Args:
            query: Search query text
            top_k: Number of results to return

        Returns:
            List of dictionaries containing matching documents
        """
        # Generate embedding for the query
        query_embedding = self.generate_embedding(query)
        query_bytes = np.array(query_embedding, dtype=np.float32).tobytes()

        # Create the search query
        # KNN = K-Nearest Neighbors search
        q = Query(f"*=>[KNN {top_k} @embedding $vec AS score]")\
            .return_fields("text", "source", "chunk_id", "score")\
            .sort_by("score")\
            .dialect(2)

        # Execute search
        results = self.redis_client.ft(self.index_name).search(
            q,
            query_params={"vec": query_bytes}
        )

        # Format results
        documents = []
        for doc in results.docs:
            documents.append({
                "text": doc.text,
                "source": doc.source,
                "chunk_id": doc.chunk_id,
                "score": float(doc.score)  # Cosine similarity score
            })

        return documents


# Example usage
if __name__ == "__main__":
    # Initialize indexer
    indexer = DocumentIndexer()

    # Sample document about Redis
    redis_doc = """
    Redis is an open-source, in-memory data structure store used as a database,
    cache, message broker, and streaming engine. Redis provides data structures
    such as strings, hashes, lists, sets, sorted sets with range queries, bitmaps,
    hyperloglogs, geospatial indexes, and streams.

    Redis has built-in replication, Lua scripting, LRU eviction, transactions,
    and different levels of on-disk persistence, and provides high availability
    via Redis Sentinel and automatic partitioning with Redis Cluster.

    RedisSearch is a Redis module that provides querying, secondary indexing,
    and full-text search for Redis. It supports vector similarity search, making
    it perfect for building RAG applications and semantic search systems.
    """

    rag_doc = """
    Retrieval-Augmented Generation (RAG) is a technique that combines information
    retrieval with text generation. When a user asks a question, the system first
    retrieves relevant documents from a knowledge base, then uses those documents
    as context for a language model to generate an answer.

    RAG solves the problem of LLMs hallucinating or providing outdated information.
    Instead of relying solely on the model's training data, RAG grounds responses
    in retrieved facts. This makes it ideal for question-answering systems,
    chatbots, and knowledge management applications.

    The key components of RAG are: document chunking, embedding generation,
    vector similarity search, and context-aware generation. The quality of
    retrieval directly impacts the quality of generation.
    """

    # Index documents
    indexer.index_document(redis_doc, "redis_overview.txt")
    indexer.index_document(rag_doc, "rag_explained.txt")

    # Search
    print("\n" + "="*60)
    print("SEARCHING FOR: 'What is vector similarity search?'")
    print("="*60)

    results = indexer.search("What is vector similarity search?", top_k=2)

    for i, result in enumerate(results, 1):
        print(f"\nResult {i} (Score: {result['score']:.4f}):")
        print(f"Source: {result['source']}")
        print(f"Text: {result['text'][:200]}...")
```

### Creating the RAG Query Engine

Now let's create the component that ties everything together—retrieving relevant documents and generating answers using Mistral:

```python
import ollama
from typing import List, Dict

class RAGEngine:
    def __init__(self, indexer: DocumentIndexer, model: str = "mistral"):
        """
        Initialize the RAG engine.

        Args:
            indexer: DocumentIndexer instance for retrieving documents
            model: Ollama model name to use for generation
        """
        self.indexer = indexer
        self.model = model

    def create_context(self, documents: List[Dict]) -> str:
        """
        Combine retrieved documents into a context string.

        We include source information so the model can cite sources
        in its response.
        """
        context_parts = []
        for i, doc in enumerate(documents, 1):
            context_parts.append(
                f"[Document {i} from {doc['source']}]:\n{doc['text']}\n"
            )
        return "\n".join(context_parts)

    def generate_prompt(self, question: str, context: str) -> str:
        """
        Create a prompt that instructs the model to answer based on context.

        This prompt engineering is crucial - we explicitly tell the model
        to use only the provided context and to admit when it doesn't know.
        """
        prompt = f"""You are a helpful assistant answering questions based on provided context.

Context:
{context}

Question: {question}

Instructions:
1. Answer the question using ONLY the information from the context above
2. If the context doesn't contain enough information to answer, say so
3. Be specific and cite which document you're using when possible
4. Keep your answer concise but complete

Answer:"""
        return prompt

    def query(self, question: str, top_k: int = 3, verbose: bool = True) -> Dict:
        """
        Answer a question using RAG.

        Args:
            question: User's question
            top_k: Number of documents to retrieve
            verbose: Whether to print intermediate steps

        Returns:
            Dictionary with answer and retrieved documents
        """
        # Step 1: Retrieve relevant documents
        if verbose:
            print(f"Searching for relevant documents...")

        documents = self.indexer.search(question, top_k=top_k)

        if not documents:
            return {
                "answer": "I couldn't find any relevant information to answer your question.",
                "documents": [],
                "question": question
            }

        if verbose:
            print(f"Found {len(documents)} relevant documents")
            for i, doc in enumerate(documents, 1):
                print(f"  {i}. {doc['source']} (similarity: {doc['score']:.4f})")

        # Step 2: Create context from retrieved documents
        context = self.create_context(documents)

        # Step 3: Generate prompt
        prompt = self.generate_prompt(question, context)

        if verbose:
            print(f"\nGenerating answer using {self.model}...")

        # Step 4: Generate answer using LLM
        response = ollama.generate(
            model=self.model,
            prompt=prompt,
            options={
                "temperature": 0.7,  # Slightly creative but mostly factual
                "top_p": 0.9,
            }
        )

        answer = response['response']

        return {
            "answer": answer,
            "documents": documents,
            "question": question
        }

    def interactive_session(self):
        """Run an interactive Q&A session."""
        print("\n" + "="*60)
        print("RAG Interactive Session")
        print("="*60)
        print("Ask questions about your indexed documents.")
        print("Type 'quit' or 'exit' to end the session.\n")

        while True:
            question = input("Question: ").strip()

            if question.lower() in ['quit', 'exit', 'q']:
                print("Goodbye!")
                break

            if not question:
                continue

            print()
            result = self.query(question, verbose=True)

            print("\n" + "-"*60)
            print("ANSWER:")
            print("-"*60)
            print(result['answer'])
            print("\n" + "="*60 + "\n")


# Example usage
if __name__ == "__main__":
    # Initialize components
    indexer = DocumentIndexer()
    rag_engine = RAGEngine(indexer)

    # Add some sample documents
    ollama_doc = """
    Ollama is a tool that makes it easy to run large language models locally.
    It supports various models including Llama 2, Mistral, Code Llama, and more.

    Ollama handles all the complexity of running LLMs - it manages model downloads,
    provides GPU acceleration when available, and exposes a simple API for
    interacting with models. You can run models via the command line or through
    its REST API.

    Models in Ollama are specified using a Modelfile, similar to a Dockerfile.
    You can customize parameters like temperature, context window size, and
    system prompts. Ollama runs on macOS, Linux, and Windows.
    """

    embeddings_doc = """
    Embeddings are dense vector representations of data, typically text, that
    capture semantic meaning. Modern embedding models use transformer architectures
    to convert text into high-dimensional vectors (often 384, 768, or 1536 dimensions).

    The key property of embeddings is that semantically similar content has similar
    vectors. This enables semantic search - finding documents by meaning rather than
    just keyword matching.

    Popular embedding models include OpenAI's text-embedding-ada-002, Sentence-BERT,
    and nomic-embed-text. These models are trained on large corpora to learn semantic
    relationships between words, phrases, and documents.
    """

    # Index documents
    indexer.index_document(ollama_doc, "ollama_guide.txt")
    indexer.index_document(embeddings_doc, "embeddings_explained.txt")

    # Run some example queries
    questions = [
        "How do I run models locally?",
        "What are embeddings used for?",
        "What is semantic search?"
    ]

    for question in questions:
        print("\n" + "="*60)
        result = rag_engine.query(question, verbose=True)
        print("\n" + "-"*60)
        print("ANSWER:")
        print("-"*60)
        print(result['answer'])
        print()

    # Uncomment to start interactive session
    # rag_engine.interactive_session()
```

### Advanced: Handling Different Document Types

In real applications, you'll want to handle various document formats. Here's an extension that supports multiple file types:

```python
import os
from pathlib import Path
from typing import Union

class MultiFormatIndexer(DocumentIndexer):
    """Extended indexer that supports multiple document formats."""

    def load_text_file(self, file_path: str) -> str:
        """Load a plain text file."""
        with open(file_path, 'r', encoding='utf-8') as f:
            return f.read()

    def load_markdown_file(self, file_path: str) -> str:
        """Load a markdown file (basic implementation)."""
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        # You could add markdown parsing here if needed
        return content

    def load_pdf_file(self, file_path: str) -> str:
        """Load a PDF file (requires PyPDF2 or pdfplumber)."""
        try:
            import PyPDF2
            text = []
            with open(file_path, 'rb') as f:
                pdf_reader = PyPDF2.PdfReader(f)
                for page in pdf_reader.pages:
                    text.append(page.extract_text())
            return '\n'.join(text)
        except ImportError:
            raise ImportError("Install PyPDF2: pip install PyPDF2")

    def index_file(self, file_path: Union[str, Path]):
        """
        Index a file, automatically detecting its format.

        Supports: .txt, .md, .pdf
        """
        file_path = Path(file_path)

        if not file_path.exists():
            raise FileNotFoundError(f"File not found: {file_path}")

        # Load content based on file extension
        ext = file_path.suffix.lower()

        if ext == '.txt':
            content = self.load_text_file(str(file_path))
        elif ext == '.md':
            content = self.load_markdown_file(str(file_path))
        elif ext == '.pdf':
            content = self.load_pdf_file(str(file_path))
        else:
            raise ValueError(f"Unsupported file type: {ext}")

        # Index the document
        self.index_document(content, source=file_path.name)

    def index_directory(self, directory: Union[str, Path], recursive: bool = True):
        """
        Index all supported files in a directory.

        Args:
            directory: Path to directory
            recursive: Whether to search subdirectories
        """
        directory = Path(directory)

        if not directory.is_dir():
            raise NotADirectoryError(f"Not a directory: {directory}")

        pattern = "**/*" if recursive else "*"
        supported_extensions = {'.txt', '.md', '.pdf'}

        files_indexed = 0
        for file_path in directory.glob(pattern):
            if file_path.is_file() and file_path.suffix.lower() in supported_extensions:
                try:
                    print(f"\nIndexing: {file_path}")
                    self.index_file(file_path)
                    files_indexed += 1
                except Exception as e:
                    print(f"Error indexing {file_path}: {e}")

        print(f"\nIndexed {files_indexed} files from {directory}")


# Example usage
if __name__ == "__main__":
    indexer = MultiFormatIndexer()

    # Index a directory of documents
    # indexer.index_directory("./my_documents", recursive=True)

    # Or index individual files
    # indexer.index_file("./README.md")
    # indexer.index_file("./documentation.pdf")
```

## Performance Optimization and Best Practices

### 1. Choosing the Right Chunk Size

Chunk size significantly impacts RAG performance. Too small, and you lose context. Too large, and you dilute relevance.

**Guidelines:**
- **Technical documentation**: 300-500 words
- **Narrative text**: 500-1000 words
- **Code**: 50-100 lines or by function/class

Always use overlap (50-100 words) to preserve context at boundaries.

### 2. Vector Index Types

RedisSearch supports two vector index types:

**FLAT Index:**
- Brute-force search through all vectors
- Guarantees exact results
- Fast for datasets < 10,000 documents
- Simple to configure

**HNSW Index (Hierarchical Navigable Small World):**
- Approximate nearest neighbor search
- Much faster for large datasets
- Slightly less accurate but practically equivalent
- Best for datasets > 50,000 documents

To use HNSW, change the index creation:

```python
VectorField(
    "embedding",
    "HNSW",
    {
        "TYPE": "FLOAT32",
        "DIM": self.vector_dim,
        "DISTANCE_METRIC": "COSINE",
        "M": 16,  # Number of bi-directional links per node
        "EF_CONSTRUCTION": 200,  # Size of dynamic candidate list
    }
),
```

### 3. Reranking Results

For better accuracy, implement a two-stage retrieval:

1. **First pass**: Retrieve top 10-20 results using vector similarity
2. **Second pass**: Rerank using a cross-encoder model for better precision

```python
def rerank_results(self, query: str, documents: List[Dict], top_k: int = 3) -> List[Dict]:
    """
    Rerank results using a more sophisticated model.
    This is more accurate but slower than pure vector search.
    """
    # In practice, you'd use a cross-encoder model here
    # For now, we'll just return top_k
    return documents[:top_k]
```

### 4. Caching Embeddings

Generate embeddings once and cache them:

```python
import json
from functools import lru_cache

class CachedIndexer(DocumentIndexer):
    def __init__(self, *args, cache_file='embedding_cache.json', **kwargs):
        super().__init__(*args, **kwargs)
        self.cache_file = cache_file
        self.cache = self._load_cache()

    def _load_cache(self):
        try:
            with open(self.cache_file, 'r') as f:
                return json.load(f)
        except FileNotFoundError:
            return {}

    def _save_cache(self):
        with open(self.cache_file, 'w') as f:
            json.dump(self.cache, f)

    def generate_embedding(self, text: str) -> List[float]:
        # Use text hash as cache key
        cache_key = hashlib.md5(text.encode()).hexdigest()

        if cache_key in self.cache:
            return self.cache[cache_key]

        # Generate new embedding
        embedding = super().generate_embedding(text)

        # Cache it
        self.cache[cache_key] = embedding
        self._save_cache()

        return embedding
```

### 5. Monitoring and Debugging

Add logging to understand retrieval quality:

```python
def query(self, question: str, top_k: int = 3, verbose: bool = True) -> Dict:
    """Enhanced query with detailed logging."""

    # Log the question
    print(f"\n{'='*60}")
    print(f"Question: {question}")
    print(f"{'='*60}")

    # Retrieve and log
    documents = self.indexer.search(question, top_k=top_k)

    print(f"\nRetrieved {len(documents)} documents:")
    for i, doc in enumerate(documents, 1):
        print(f"\n[{i}] Similarity: {doc['score']:.4f} | Source: {doc['source']}")
        print(f"Preview: {doc['text'][:150]}...")

    # Check if similarity scores are too low
    if documents and documents[0]['score'] < 0.5:
        print("\n⚠️  Warning: Low similarity scores. Retrieved documents may not be relevant.")

    # Rest of the implementation...
```

## Common Pitfalls and Solutions

### 1. Poor Retrieval Quality

**Problem**: Retrieved documents aren't relevant to the query.

**Solutions**:
- Verify embeddings are being generated correctly
- Check if chunk size is appropriate for your content
- Increase `top_k` to retrieve more candidates
- Try different embedding models
- Add query expansion (generate multiple related queries)

### 2. Out-of-Memory Errors

**Problem**: System runs out of memory with large datasets.

**Solutions**:
- Use batch processing for indexing
- Switch to HNSW index for large datasets
- Run smaller models (e.g., `mistral:7b-instruct-q4_0`)
- Increase system swap space

### 3. Slow Query Times

**Problem**: Queries take too long to complete.

**Solutions**:
- Use HNSW index instead of FLAT
- Reduce embedding dimensions (trade accuracy for speed)
- Cache embeddings for common queries
- Use quantized models (q4, q5 variants)
- Limit `top_k` to what you actually need

### 4. Hallucination Despite RAG

**Problem**: Model still hallucinates even with context.

**Solutions**:
- Improve prompt engineering (be more explicit)
- Lower temperature (more deterministic)
- Use instruction-tuned models
- Add explicit constraints in the prompt
- Implement answer verification

## Conclusion

We've built a complete RAG system from the ground up, covering everything from the theoretical foundations of embeddings and vector similarity to a practical implementation using RedisSearch and Ollama. You now have a production-ready system running entirely on your local machine, with no external API calls or data leaving your computer.

The key takeaways:

1. **Embeddings** convert semantic meaning into mathematical vectors that machines can compare
2. **Cosine similarity** is the standard for comparing embeddings because it measures orientation rather than magnitude
3. **RedisSearch** provides a powerful, scalable vector database with minimal setup
4. **Ollama** makes running LLMs locally accessible and practical
5. **RAG** combines retrieval and generation to ground LLM responses in factual information

This is just the beginning. You can extend this system by adding more sophisticated chunking strategies, implementing hybrid search (combining keyword and vector search), adding conversation memory, or building a web interface.

The beauty of this setup is that it's completely under your control. Your data stays local, you can customize every component, and you're not subject to API rate limits or costs. As models continue to improve and your dataset grows, this architecture scales with you.

Start experimenting with your own documents, and you'll quickly see how powerful RAG can be for making LLMs truly useful for your specific domain and use cases.
