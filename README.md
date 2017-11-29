# agenda
agenda实现思路参考
1. https://console.bluemix.net/docs/services/conversation/expression-language.html#accessing-and-evaluating-objects
2. UIMA CPE/AP

agenda是一个从来做task based dialogue的对话引擎。其核心思想是用脚本语言配置一个workflow，agenda引擎将会读入这个workflow，并执行。数据的流转将由一个KB引擎进行负责管理。

agenda包含以下模块:
1. 知识管理模块
   ontop做关系管理，mysql/postsql做存储

2. 脚本执行引擎
https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions

3. 事件流分发引擎
https://github.com/ReactiveX/RxJava

4. 协程管理引擎
一个对话由一个协程分管，最大化并发和性能优化

5. 任务调度引擎
一个对话执行流程中可以分化出n个并行流程，例如可以同时调用KBQA，IRQA，etc

agenda支持以下功能点：
1. 问答对是多对多关系

