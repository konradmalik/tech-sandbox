package graphql

import sangria.execution.deferred.{ DeferredResolver, Fetcher, Relation, RelationIds }
import sangria.schema._

object SchemaDef {

  import Models._
  import sangria.macros.derive._

  /**
   * Relations
   */
  private val categoriesForProductRelation = Relation[Category, (Seq[ProductId], Category), ProductId]("p-c", _._1, _._2)
  private val productsForCategoryRelation = Relation[Product, (Seq[CategoryId], Product), CategoryId]("c-p", _._1, _._2)

  /**
   * Fetchers
   */
  private val productsFetcher: Fetcher[ShopRepository, Product, (Seq[ProductId], Product), ProductId] = Fetcher.relCaching(
    (ctxRepo: ShopRepository, ids: Seq[ProductId]) => ctxRepo.products(ids),
    (ctxRepo: ShopRepository, ids: RelationIds[Product]) => ctxRepo.productsByCategories(ids(productsForCategoryRelation)))

  private val categoriesFetcher: Fetcher[ShopRepository, Category, (Seq[CategoryId], Category), CategoryId] = Fetcher.relCaching(
    (repo: ShopRepository, ids: Seq[CategoryId]) => repo.categories(ids),
    (repo: ShopRepository, ids: RelationIds[Category]) => repo.categoriesByProducts(ids(categoriesForProductRelation)))

  /**
   * Resolvers
   */
  val deferredResolver: DeferredResolver[ShopRepository] = DeferredResolver.fetchers(productsFetcher, categoriesFetcher)

  /**
   * Types
   */
  // elaborate schema definition
  private val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",
    fields[Unit, Identifiable](
      Field("id", IntType, resolve = _.value.id)))

  private implicit val PictureType: ObjectType[Unit, Picture] =
    deriveObjectType[Unit, Picture](
      ObjectTypeDescription("The product picture"),
      DocumentField("url", "Picture CDN URL"))

  private implicit val CategoryType: ObjectType[Unit, Category] =
    deriveObjectType[Unit, Category](
      Interfaces(IdentifiableType),
      ObjectTypeDescription("The category of products"),
      AddFields(
        Field("products", ListType(ProductType),
          complexity = constantComplexity(30),
          resolve = p => productsFetcher.deferRelSeq(productsForCategoryRelation, p.value.id))))

  // macro used just because we can. Macros need case classes
  private implicit val ProductType: ObjectType[Unit, Product] =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture"), //by default macro considers fields only,
      AddFields(
        Field("categories", ListType(CategoryType),
          complexity = constantComplexity(30),
          resolve = p => categoriesFetcher.deferRelSeq(categoriesForProductRelation, p.value.id))))

  private val IdArg = Argument("id", IntType)
  private val NameArg = Argument("name", StringType)

  private val QueryType = ObjectType(
    "Query",
    fields[ShopRepository, Unit](
      Field("allProducts", ListType(ProductType),
        description = Some("Returns a list of all available products."),
        complexity = constantComplexity(100),
        resolve = _.ctx.allProducts),
      Field("product", OptionType(ProductType),
        description = Some("Returns a product with specific `id`."),
        arguments = IdArg :: Nil,
        resolve = c => productsFetcher.defer(c.arg[ProductId]("id"))),
      Field("products", ListType(ProductType),
        description = Some("Returns a list of products for provided IDs."),
        arguments = Argument("ids", ListInputType(IntType)) :: Nil,
        resolve = c => productsFetcher.deferSeq(c.arg[List[ProductId]]("ids"))),
      Field("allCategories", ListType(CategoryType),
        description = Some("Returns a list of all available categories."),
        complexity = constantComplexity(250),
        resolve = _.ctx.allCategories),
      Field("category", OptionType(CategoryType),
        description = Some("Returns a category with specific `id`."),
        arguments = IdArg :: Nil,
        resolve = c => categoriesFetcher.defer(c.arg[CategoryId]("id"))),
      Field("categories", ListType(CategoryType),
        description = Some("Returns a list of categories for provided IDs."),
        complexity = constantComplexity(30),
        arguments = Argument("ids", ListInputType(IntType)) :: Nil,
        resolve = c => categoriesFetcher.deferSeq(c.arg[List[CategoryId]]("ids")))))

  private val MutationType = ObjectType(
    "Mutation",
    fields[ShopRepository, Unit](
      Field("addCategory", CategoryType,
        arguments = IdArg :: NameArg :: Nil,
        resolve = c => c.ctx.addCategory(c.arg(IdArg), c.arg(NameArg)))))

  val ShopSchema = Schema(QueryType, Some(MutationType))

  private def constantComplexity[Ctx](complexity: Double) =
    Some((_: Ctx, _: Args, child: Double) => child + complexity)
}