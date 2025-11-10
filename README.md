## ISW321 | Cleaning Robots

### ARQUITECTURA Y DISEÑO DEL SISTEMA

El sistema de robots de limpieza autónomos se ha desarrollado siguiendo un patrón de arquitectura por capas, donde la lógica de negocio se encuentra completamente desacoplada de la presentación y el acceso a datos. La capa de servicios, ubicada en `src/com/isw/app/services/`, contiene toda la inteligencia artificial y los algoritmos que permiten a los robots tomar decisiones autónomas, planificar rutas, gestionar su energía y coordinar sus acciones entre múltiples agentes.

### SERVICIO DE CÁLCULO (CalculationService)

El `CalculationService` es el cerebro matemático del sistema. Este servicio es responsable de determinar cuántos robots son necesarios para limpiar eficientemente un salón dado. El algoritmo implementado analiza tres variables principales: el tamaño total del salón (filas multiplicadas por columnas), la cantidad de obstáculos permanentes y temporales que bloquean el paso, y el porcentaje de suciedad presente en el ambiente.

La lógica detrás del cálculo de robots considera que un espacio más grande requiere más agentes de limpieza, pero este número se ajusta dinámicamente según la complejidad del entorno. Si hay muchos obstáculos, los robots tendrán dificultades para moverse libremente, por lo que se necesitan más unidades para compensar las rutas bloqueadas y los caminos más largos. De igual manera, un alto porcentaje de suciedad incrementa la carga de trabajo, requiriendo robots adicionales para alcanzar el objetivo del 80% de limpieza en un tiempo razonable.

El algoritmo implementa una fórmula ponderada donde cada factor contribuye proporcionalmente al número final de robots. Primero se calcula un factor base dividiendo el área total del salón entre un umbral predefinido que representa cuántas celdas puede manejar eficientemente un solo robot. Luego, este factor se multiplica por coeficientes que representan el impacto de los obstáculos y la suciedad. El resultado se redondea hacia arriba para asegurar que siempre haya suficientes robots, y se valida contra límites mínimos y máximos para evitar casos extremos donde haya demasiados pocos o demasiados robots operando simultáneamente.

### SERVICIO DE ASIGNACIÓN (AssignmentService)

El `AssignmentService` trabaja en conjunto con el servicio de cálculo para posicionar inicialmente los robots en el salón. Este servicio implementa un algoritmo de distribución espacial que busca colocar los robots de manera óptima, maximizando la cobertura del área y minimizando la distancia inicial a los puntos sucios.

El algoritmo de asignación comienza identificando todas las celdas vacías disponibles en el salón, es decir, aquellas que no contienen obstáculos permanentes, obstáculos temporales, puntos de recarga ni suciedad. Una vez identificadas estas posiciones candidatas, el sistema evalúa cada una utilizando un sistema de puntuación que considera múltiples factores: la distancia promedio a todos los espacios sucios del salón, la cercanía al punto de recarga más próximo, y la separación respecto a otros robots ya posicionados.

La estrategia implementada busca un equilibrio entre distribución uniforme y eficiencia operativa. Si todos los robots se colocan en el mismo sector del salón, perderán tiempo moviéndose hacia áreas lejanas y competirán entre sí por los mismos espacios sucios. Por otro lado, si se distribuyen demasiado separados, algunos podrían quedar aislados lejos de las áreas que requieren limpieza. El algoritmo utiliza un enfoque iterativo donde cada robot se coloca uno a la vez, recalculando las puntuaciones de las posiciones disponibles después de cada asignación para considerar la nueva configuración del sistema.

### SERVICIO DE COORDENADAS (CoordinateService)

El `CoordinateService` proporciona las operaciones matemáticas fundamentales para trabajar con el espacio bidimensional del salón. Este servicio encapsula toda la lógica relacionada con la manipulación de coordenadas, el cálculo de distancias y la validación de posiciones.

Una de las funciones más críticas de este servicio es el cálculo de distancia Manhattan, también conocida como distancia de taxista o distancia L1. A diferencia de la distancia euclidiana que considera la línea recta entre dos puntos, la distancia Manhattan calcula cuántos movimientos ortogonales (arriba, abajo, izquierda, derecha) se necesitan para ir de un punto a otro. Esta métrica es perfecta para nuestro sistema porque los robots solo pueden moverse en las cuatro direcciones cardinales, no en diagonal.

El servicio también implementa funciones para obtener todas las celdas adyacentes a una posición dada, validar si una coordenada está dentro de los límites del salón, y calcular vectores de dirección entre posiciones. Estas operaciones son utilizadas constantemente por otros servicios para tomar decisiones de movimiento, evaluar proximidad entre robots, y planificar rutas.

### SERVICIO DE BÚSQUEDA DE CAMINOS (PathfindingService)

El `PathfindingService` implementa el algoritmo A* (A-estrella), uno de los algoritmos de búsqueda de caminos más eficientes y utilizados en inteligencia artificial. Este algoritmo permite a los robots encontrar la ruta óptima entre su posición actual y un destino, evitando obstáculos y minimizando la distancia recorrida.

El algoritmo A* funciona manteniendo dos estructuras de datos principales: una lista abierta de nodos por explorar y una lista cerrada de nodos ya evaluados. Cada nodo representa una celda del salón y contiene tres valores críticos: el costo G que representa la distancia real desde el punto de inicio hasta ese nodo, el costo H que es una estimación heurística de la distancia desde ese nodo hasta el destino (calculada usando distancia Manhattan), y el costo F que es la suma de G más H.

El proceso comienza agregando la posición inicial del robot a la lista abierta. En cada iteración, el algoritmo selecciona el nodo con el menor costo F de la lista abierta, lo marca como visitado moviéndolo a la lista cerrada, y examina todos sus vecinos adyacentes. Para cada vecino que no sea un obstáculo ni esté en la lista cerrada, el algoritmo calcula su costo G sumando el costo del nodo actual más uno (cada movimiento cuesta una unidad de energía). Si este vecino no está en la lista abierta, se agrega con sus costos calculados. Si ya está en la lista abierta pero el nuevo camino tiene un costo G menor, se actualizan sus valores porque se ha encontrado una ruta más eficiente.

El algoritmo continúa expandiendo nodos hasta encontrar el destino o determinar que no existe un camino válido. Una vez encontrado el destino, se reconstruye la ruta óptima siguiendo los punteros de padre de cada nodo desde el destino hasta el origen. Esta ruta representa la secuencia exacta de movimientos que el robot debe ejecutar para llegar a su objetivo minimizando el consumo de batería.

Una característica importante de la implementación es que el algoritmo considera el estado de la batería del robot. Antes de aceptar una ruta, el sistema valida que el robot tenga suficiente energía no solo para llegar al destino, sino también para regresar a un punto de recarga si es necesario. Esta validación preventiva evita que los robots queden inactivos en medio del salón sin batería.

### SERVICIO DE NAVEGACIÓN (NavigationService)

El `NavigationService` actúa como el director de operaciones de cada robot, coordinando todos los aspectos de la toma de decisiones de alto nivel. Este servicio integra la información de múltiples fuentes para determinar qué debe hacer el robot en cada turno: ¿debe moverse hacia un espacio sucio? ¿Debe ir a recargar? ¿Debe esperar porque hay otro robot cerca? ¿Debe buscar una ruta alternativa?

El proceso de decisión comienza evaluando el estado actual del robot. Si el robot tiene batería baja (menos del 30% de su capacidad máxima), el servicio inmediatamente prioriza buscar el punto de recarga más cercano, utilizando el `PathfindingService` para encontrar la ruta óptima. El umbral del 30% no es arbitrario; está calculado para asegurar que el robot tenga suficiente energía para llegar al punto de recarga desde casi cualquier posición del salón, incluyendo un margen de seguridad para desvíos por obstáculos.

Si el robot tiene batería suficiente, el servicio procede a identificar el espacio sucio más prometedor para limpiar. Esta decisión no se basa únicamente en la distancia; el algoritmo implementa un sistema de puntuación multifactorial. Cada espacio sucio recibe una puntuación basada en: su distancia al robot (espacios más cercanos tienen mayor prioridad), la cantidad de obstáculos en el camino hacia él (rutas más despejadas son preferibles), la presencia de otros robots cercanos (se evitan espacios donde ya hay otro robot trabajando), y la accesibilidad (espacios rodeados de obstáculos tienen menor prioridad).

El servicio también implementa lógica de coordinación entre robots. Antes de comprometerse con un objetivo, el robot verifica si hay otros robots más cercanos a ese espacio sucio o con mayor batería. Si existe un robot mejor posicionado, el robot actual busca un objetivo alternativo para evitar redundancia y mejorar la eficiencia global del sistema. Esta coordinación implícita, sin comunicación directa entre robots, emerge de las reglas de prioridad implementadas en el servicio.

Otro aspecto crítico es el manejo de obstáculos temporales. Cuando un robot encuentra un obstáculo temporal en su ruta planificada, el `NavigationService` evalúa si vale la pena esperar a que desaparezca o si es mejor buscar una ruta alternativa. Esta decisión considera el tiempo restante del obstáculo temporal, la longitud de la ruta alternativa, y la batería disponible del robot. Si el obstáculo desaparecerá pronto y la ruta alternativa es significativamente más larga, el robot decide esperar; de lo contrario, recalcula su ruta.

### SERVICIO DE MOVIMIENTO (MovementService)

El `MovementService` es el ejecutor táctico que traduce las decisiones estratégicas del `NavigationService` en acciones concretas. Este servicio maneja la mecánica de mover el robot de una celda a otra, validando que el movimiento sea legal y actualizando todos los estados correspondientes.

Antes de ejecutar cualquier movimiento, el servicio realiza una serie de validaciones exhaustivas. Primero verifica que la celda destino esté dentro de los límites del salón, utilizando el `CoordinateService` para esta validación geométrica. Luego confirma que la celda destino no contenga un obstáculo permanente ni un obstáculo temporal activo. También verifica que no haya otro robot ocupando esa posición, previniendo colisiones entre agentes.

Una validación crucial es la verificación de batería. El servicio confirma que el robot tenga al menos una unidad de carga disponible antes de permitir el movimiento. Si el robot intenta moverse sin batería suficiente, el movimiento se cancela y el robot cambia a estado inactivo. Esta validación es esencial porque un robot sin batería fuera de un punto de recarga queda permanentemente inutilizado.

Una vez validado el movimiento, el servicio actualiza la posición del robot en el modelo y decrementa su batería en una unidad. El servicio también notifica a otros componentes del sistema sobre el movimiento ejecutado, permitiendo que la interfaz gráfica actualice la visualización y que el sistema de registro documente la acción.

El `MovementService` también maneja casos especiales como el movimiento hacia puntos de recarga. Cuando un robot se mueve a una celda que contiene un punto de recarga, el servicio coordina con el sistema de gestión de energía para iniciar el proceso de recarga automática.

### SERVICIO DE LIMPIEZA (CleaningService)

El `CleaningService` encapsula toda la lógica relacionada con la acción principal de los robots: limpiar espacios sucios. Aunque conceptualmente simple, este servicio implementa varios mecanismos de validación y coordinación importantes.

Cuando un robot se posiciona sobre un espacio sucio, el servicio primero valida que efectivamente haya suciedad en esa celda. Esta validación puede parecer redundante, pero es necesaria porque otro robot podría haber limpiado ese espacio entre el momento en que el robot actual planificó su ruta y el momento en que llegó a destino.

Antes de limpiar, el servicio verifica la proximidad de otros robots. Si hay otro robot en una celda adyacente, el sistema evalúa cuál de los dos tiene mayor prioridad para limpiar ese espacio. La prioridad se determina primero por quién llegó primero (el robot que ya está sobre la celda sucia tiene prioridad absoluta), y en caso de que ambos estén en celdas adyacentes evaluando el mismo objetivo, se considera quién tiene más batería o está mejor posicionado estratégicamente.

El proceso de limpieza en sí es instantáneo una vez validado. El servicio cambia el estado de la celda de sucia a limpia, incrementa el contador de espacios limpiados por ese robot específico, y actualiza las estadísticas globales del sistema. Esta actualización de estadísticas es crucial para determinar si se ha alcanzado el objetivo del 80% de limpieza.

El `CleaningService` también mantiene un registro histórico de qué robot limpió cada espacio y en qué momento. Esta información es utilizada posteriormente para generar los reportes finales y el archivo `registro.txt` que documenta toda la operación de limpieza.

### INTEGRACIÓN Y COORDINACIÓN DE SERVICIOS

Todos estos servicios no trabajan de manera aislada; están diseñados para colaborar en un ecosistema coherente. El flujo típico de un ciclo de decisión de un robot involucra múltiples servicios trabajando en secuencia: el `NavigationService` determina qué hacer, el `PathfindingService` calcula cómo llegar allí, el `CoordinateService` valida las posiciones, el `MovementService` ejecuta el desplazamiento, y el `CleaningService` realiza la limpieza cuando es apropiado.

Esta arquitectura modular permite que cada servicio se especialice en un aspecto específico del problema, manteniendo la complejidad bajo control y facilitando el mantenimiento y la extensión del sistema. Cada servicio tiene responsabilidades claramente definidas y se comunica con otros servicios a través de interfaces bien establecidas, siguiendo los principios SOLID de diseño orientado a objetos.

La coordinación entre servicios se gestiona principalmente a través del modelo de datos compartido, donde cada servicio lee y actualiza el estado del sistema de manera controlada. Este diseño permite que cambios en una parte del sistema se propaguen automáticamente a otras partes que dependen de esa información, manteniendo la consistencia global sin necesidad de acoplamiento directo entre servicios.