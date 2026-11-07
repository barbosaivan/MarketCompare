# Sistema de diseño — Material Design 3

Estado: PLANNED hasta implementar tokens.

Colores desde MaterialTheme.colorScheme; tipografía desde MaterialTheme.typography; shapes, espaciado y elevaciones desde tokens centralizados. Textos visibles mediante recursos localizados, idioma inicial español (Colombia). Tema claro/oscuro, accesibilidad, TalkBack y escalado de texto.

Componentes candidatos: AppScaffold, PriceCard, SourceBadge, MoneyText, ProductRow, ReceiptItemEditor, ConfidenceIndicator, EmptyState, LoadingState, ErrorState, OfflineState y SearchField.

No hardcodear strings ni colores en composables. No crear abstracciones visuales sin reutilización real.
