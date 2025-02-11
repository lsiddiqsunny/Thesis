# Notes for paper on Tree LSTM

Collected paper on tree LSTM and notes on them.

1. **Improved Semantic Representations FromTree-Structured Long Short-Term Memory Networks**

    *Abstract*

    ---

    ```Because  of  their  superior  ability  to  pre-serve   sequence   information   over   time,Long  Short-Term  Memory  (LSTM)  net-works,   a  type  of  recurrent  neural  net-work with a more complex computationalunit, have obtained strong results on a va-riety  of  sequence  modeling  tasks.Theonly underlying LSTM structure that hasbeen  explored  so  far  is  a  linear  chain.However,  natural  language  exhibits  syn-tactic properties that would naturally com-bine words to phrases.  We introduce theTree-LSTM, a generalization of LSTMs totree-structured network topologies.  Tree-LSTMs  outperform  all  existing  systemsand strong LSTM baselines on two tasks:predicting the semantic relatedness of twosentences  (SemEval  2014,  Task  1)  andsentiment  classification  (Stanford  Senti-ment Treebank)```

    *My notes*

    ---

   Most models for distributed representations of phrases and sentences—that is, models where realvalued vectors are used to represent meaning—fall into one of three classes: **bag-of-words models**,**sequence models**, and **tree-structured models.**

   In this paper, they introduce a generalization of the standard LSTM architecture to **tree-structured network topologies** and show its superiority for representing sentence meaning over a sequential LSTM.

   The **standard LSTM** can then be considered a **special case** of the **Tree-LSTM** where each internal node has exactly **one child**.

   Recurrent neural networks (RNNs) are able to process input sequences of arbitrary length via the recursive application of a transition function on a *hidden state vector h<sub>t</sub>*. At each time step t, the hidden state *h<sub>t</sub>* is a function of the input vector *x<sub>t</sub>* that the network receives at time t and its previous hidden state *h<sub>t</sub>-1.*

   Unfortunately, a problem with RNNs with transition functions of this form is that during training,components of the gradient vector can grow or decay exponentially over long sequences. This problem with exploding or vanishing gradients makes it difficult for the RNN model to learn long-distance correlations in a sequence.

   The LSTM architecture addresses this problem of learning long-term dependencies by introducing a memory cell that is able to preserve state over long periods of time.

   As in standard LSTM units, each Tree-LSTM unit (indexed by *j*) contains input and output gates i<sub>j</sub> and o<sub>j</sub> , a memory cell c<sub>j</sub> and hidden state h<sub>j</sub> . The difference between the standard LSTM unit and Tree-LSTM units is that gating vectors and memory cell updates are dependent on the states of possibly many child units. Additionally, instead of a single forget gate, the Tree- LSTM unit contains one forget gate f<sub>jk</sub> for each child k. This allows the Tree-LSTM unit to selectively incorporate information from each child. For example, a Tree-LSTM model can learn to emphasize semantic heads in a semantic relatedness task, or it can learn to preserve the representation of sentiment-rich children for sentiment classification.

    As with the standard LSTM, each Tree-LSTM unit takes an input vector *x<sub>j</sub>*. In our applications, each *x<sub>j</sub>* is a vector representation of a **word in a sentence**. The input word at each node depends on the tree structure used for the network. For instance, in a Tree-LSTM over a dependency tree, each node in the tree takes the vector corresponding to the head word as input, whereas in a Tree-LSTM over a constituency tree, the leaf nodes take the corresponding word vectors as input.

    They evaluate their Tree-LSTM architectures on two tasks: (1) *sentiment classification of sentences sampled from movie reviews* and (2) *predicting the semantic relatedness of sentence pairs.*

2. **Tree-to-tree Neural Networks for Program Translation**

   *Abstract*

    ---
   ```Program translation is an important tool to migrate legacy code in one language into an ecosystem built in a different language. In this work, we are the first to employ deep neural networks toward tackling this problem. We observe that program translation is a modular procedure, in which a sub-tree of the source tree is translated into the corresponding target sub-tree at each step. To capture this intuition, we design a tree-to-tree neural network to translate a source tree into a target one. Meanwhile, we develop an attention mechanism for the tree-to-tree model, so that when the decoder expands one non-terminal in the target tree, the attention mechanism locates the corresponding sub-tree in the source tree to guide the expansion of the decoder. We evaluate the program translation capability of our tree-to-tree model against several state-of-the-art approaches. Compared against other neural translation models, we observe that our approach is consistently better than the baselines with a margin of up to 15 points. Further, our approach can improve the previous state-of-the-art program translation approaches by a margin of 20 points on the translation of real-world projects```

   *My notes*

    ---
   Nowadays, to translate programs between different programming languages, typically programmers would manually investigate the correspondence between the grammars of the two languages, then develop a rule-based translator. However, this process can be inefficient and error-prone. In this work, we make the first attempt to examine whether we can leverage deep neural networks to build a program translator automatically.

   In this work, we observe that the main issue of an RNN that makes it hard to produce syntactically correct programs is that it entangles two sub-tasks together: (1) *learning the grammar;* and (2) *aligning the sequence with the grammar*. When these two tasks can be handled separately, the performance can typically boost. For example, Dong et al. employ a tree-based decoder to separate the two tasks. In particular, the decoder in  leverages the tree structural information to (1) *generate the nodes at the same depth of the parse tree using an LSTM decoder;* and (2) *expand a non-terminal and generate its children in the parse tree*. Such an approach has been demonstrated to achieve the state-of-the-art results on several semantic parsing tasks.

   **(Program translation).** Given two programming languages L<sub>s</sub> and L<sub>t</sub>, each being a set of instances (p<sub>k</sub>, T<sub>k</sub>), where p<sub>k</sub> is a program, and T<sub>k</sub> is its corresponding parse tree. We assume that there exists a translation oracle PI, which maps instances in L<sub>s</sub> to instances in L<sub>t</sub>. Given a dataset of instance pairs (i<sub>s</sub>; i<sub>t</sub>) such that i<sub>s</sub> belongs to L<sub>s</sub> , i<sub>t</sub> belongs to L<sub>t</sub> and PI(i<sub>s</sub>) = i<sub>t</sub>, our problem is to learn a function F that maps each i<sub>s</sub> belongs to L<sub>s</sub> into i<sub>t</sub> = PI(i<sub>s</sub>).

   They design the *tree-to-tree neural network*, which follows an *encoder-decoder framework* to encode the source tree into an embedding, and decode the embedding into the target tree. To capture the intuition of the *modular translation process*, the decoder employs an *attention mechanism* to locate the corresponding source sub-tree when expanding the non-terminal.

   We evaluate our tree-to-tree model against a *sequence-to-sequence model* , a *sequence-to-tree model*, and a *tree-to-sequence model *. Note that for a sequence-to-sequence model, there can be four variants to handle different input-output formats. For example, given a program, we can simply tokenize it into a sequence of tokens. We call this format as raw program, denoted as **P**. We can also use the parser to parse the program into a *parse tree*, and then serialize the parse tree as a sequence of tokens. Our serialization of a tree follows its depth-first traversal order, which is the same as . We call this format as parse tree, denoted as **T**. For both input and output formats, we can choose either P or T. For a *sequence-to-tree model*, we have two variants based on its input format being either P or T; note that the sequence-to-tree model generates a tree as output, and thus requires its output format to be T (unserialized). Similarly, the tree-to-sequence model has two variants, and our tree-to-tree only has one form. Therefore, we have 9 different models in our evaluation.

   In this work, they are the first to consider neural network approaches for the program translation problem, and are the first to demonstrate a successful design of *tree-to-tree neural network combining both a tree-RNN encoder and a tree-RNN decoder* for translation tasks. Extensive evaluation demonstrates that our tree-to-tree neural network outperforms several state-of-the-art models. This renders our tree-to-tree model as a promising tool toward tackling the program translation problem. In addition, they believe that their proposed tree-to-tree neural network has the potential to generalize to other tree-to-tree tasks, and they consider it as future work.

3. CODIT: Code Editing with Tree-Based NeuralMachine Translation

   **Abstract**

   ```The way developers edit day-to-day code tends to berepetitive,  often  using  existing  code  elements.  Many  researchershave tried to automate repetitive code changes by learning fromspecific  change  templates  which  are  applied  to  limited  scope.The  advancement  of  Neural  Machine  Translation  (NMT)  andthe  availability  of  vast  open-source  evolutionary  data  opens  upthe  possibility  of  automatically  learning  those  templates  fromthe  wild.  However,  unlike  natural  languages,  for  which  NMTtechniques  were  originally  devised,  source  code  and  its  changeshave   certain   properties.   For   instance,   compared   to   naturallanguage,  source  code  vocabulary  can  be  significantly  larger.Further, good changes in code do not break its syntactic structure.Thus,  deploying  state-of-the-art  NMT  models  without  adaptingthe methods to the source code domain yields sub-optimal results.To  this  end,  we  propose  a  novel  Tree  based  NMT  system  tomodel source code changes and learn code change patterns fromthe wild. We realize our model with a change suggestion engine:CODITand  train  the  model  with  more  than  30k  real-worldchanges  and  evaluate  it  on  6k  patches.  Our  evaluation  showsthe  effectiveness  of  CODITin  learning  and  suggesting  patches.CODITalso  shows  promise  generating  bug  fix  patches.```

   **My notes**

   Will be added soon.

4. Automatic Source Code Summarization with Extended Tree-LSTM

   **Abstract**

   ```Neural  machine  translation  models  are  used  to  automatically  generate  a  document  from  given  source  codesince this can be regarded as a machine translation task.  Source code summarization is one of the components forautomatic document generation, which generates a summary in natural language from given source code.  Thissuggests that techniques used in neural machine translation, such as Long Short-Term Memory (LSTM), can beused for source code summarization.  However, there is a considerable difference between source code and naturallanguage:  Source code is essentiallystructured, having loops and conditional branching, etc.  Therefore, there issome obstacle to apply known machine translation models to source code.Abstract syntax trees (ASTs) capture these structural properties and play an important role in recent machinelearning studies on source code.  Tree-LSTM is proposed as a generalization of LSTMs for tree-structured data.However, there is a critical issue when applying it to ASTs:  It cannot handle a tree that contains nodes havingan  arbitrary  number  of  children  and  their  order  simultaneously,  which  ASTs  generally  have  such  nodes.   Toaddress this issue, we propose an extension of Tree-LSTM, which we callMulti-way Tree-LSTMand apply it forsource code summarization.  As a result of computational experiments, our proposal achieved better results whencompared with several state-of-the-art techniques.```

   **My notes**

   Will be added soon.

5. Improving Tree-LSTM with Tree Attention

   **Abstract**

   ```In Natural Language Processing (NLP), we often need to extract information from tree topology.Sentence structure can be represented via a dependency tree or a constituency tree structure. For this reason, a variant of LSTMs, named Tree-LSTM, was proposed to work on tree topology. In this paper, we design a generalized attention framework for both dependence and constituency trees by encoding variants of decomposable attention inside a Tree-LSTM cell. We evaluated our models on a semantic relatedness task and achieved notable results compared to Tree-LSTM based methods with no attention as well as other neural and non-neural methods and good results compared to Tree-LSTM based methods with attention.```

   **My notes**

   Will be added soon.

6. Improving Tree-LSTM with Tree Attention

   **Abstract**

   ```Optimizing compilers, as well as other translator systems, often work by rewriting expressions ac-cording to equivalence preserving rules.  Given an input expression and its optimized form, findingthe sequence of rules that were applied is a non-trivial task.  Most of the time, the tools provideno proof, of any kind, of the equivalence between the original expression and its optimized form.In this work, we propose to reconstruct proofs of equivalence of simple mathematical expressions,after the fact, by finding paths of equivalence preserving transformations between expressions.  Wepropose to find those sequences of transformations using a search algorithm,  guided by a neuralnetwork heuristic. Using a Tree-LSTM recursive neural network, we learn a distributed representa-tion of expressions where the Manhattan distance between vectors approximately corresponds to therewrite distance between expressions. We then show how the neural network can be efficiently usedto search for transformation paths, leading to substantial gain in speed compared to an uninformedexhaustive search.  In one of our experiments, our neural-network guided search algorithm is ableto solve more instances with a 2 seconds timeout per instance than breadth-first search does with a5 minutes timeout per instance.```

   **My notes**

   Will be added soon.

7. Structure Tree-LSTM: Structure-aware Attentional Document Encoders

   **Abstract**

   ```We propose a method to create document representations that reflect their internal structure. We modify Tree-LSTMs to hierarchically merge basic elements like words and sentences into blocks of increasing complexity. Our Structure Tree-LSTM implements a hierarchical attention mechanism over individual components and combinations thereof. We thus emphasize the usefulness of Tree-LSTMs for texts larger than a sentence. We show that structure-aware encoders can be used to improve the performance of document classification. We demonstrate that our method is resilient to changes to the basic building blocks, as it performs well with both sentence and word embeddings. The Structure Tree-LSTM outperforms all the baselines on two datasets when structural clues like sections are available, but also in the presence of mere paragraphs. On a third dataset from the medical domain, our model achieves competitive performance with the state of the art. This result shows the Structure Tree-LSTM can leverage dependency relations other than text structure, such as a set of reports on the same patient.```

   **My notes**

   Will be added soon.
