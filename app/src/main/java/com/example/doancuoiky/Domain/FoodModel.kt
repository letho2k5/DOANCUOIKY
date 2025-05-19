package com.example.doancuoiky.Domain

import com.google.firebase.database.PropertyName
import java.io.Serializable

data class FoodModel(
    @get:PropertyName("BestFood") @set:PropertyName("BestFood")
    var BestFood: Boolean = false,

    @get:PropertyName("CategoryId") @set:PropertyName("CategoryId")
    var CategoryId: String = "",

    @get:PropertyName("Description") @set:PropertyName("Description")
    var Description: String = "",

    @get:PropertyName("Id") @set:PropertyName("Id")
    var Id: Int = 0,

    @get:PropertyName("ImagePath") @set:PropertyName("ImagePath")
    var ImagePath: String = "",

    @get:PropertyName("LocationId") @set:PropertyName("LocationId")
    var LocationId: Int = 0,

    @get:PropertyName("Price") @set:PropertyName("Price")
    var Price: Double = 0.0,

    @get:PropertyName("PriceId") @set:PropertyName("PriceId")
    var PriceId: Int = 0,

    @get:PropertyName("TimeId") @set:PropertyName("TimeId")
    var TimeId: Int = 0,

    @get:PropertyName("Title") @set:PropertyName("Title")
    var Title: String = "",

    @get:PropertyName("Calorie") @set:PropertyName("Calorie")
    var Calorie: Int = 0,

    @get:PropertyName("numberInCart") @set:PropertyName("numberInCart")
    var numberInCart: Int = 0,

    @get:PropertyName("Star") @set:PropertyName("Star")
    var Star: Double = 0.0,

    @get:PropertyName("TimeValue") @set:PropertyName("TimeValue")
    var TimeValue: Int = 0
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FoodModel) return false
        return Id == other.Id &&
                BestFood == other.BestFood &&
                CategoryId == other.CategoryId &&
                Description == other.Description &&
                ImagePath == other.ImagePath &&
                LocationId == other.LocationId &&
                Price == other.Price &&
                PriceId == other.PriceId &&
                TimeId == other.TimeId &&
                Title == other.Title &&
                Calorie == other.Calorie &&
                numberInCart == other.numberInCart &&
                Star == other.Star &&
                TimeValue == other.TimeValue
    }

    override fun hashCode(): Int {
        var result = BestFood.hashCode()
        result = 31 * result + CategoryId.hashCode()
        result = 31 * result + Description.hashCode()
        result = 31 * result + Id
        result = 31 * result + ImagePath.hashCode()
        result = 31 * result + LocationId
        result = 31 * result + Price.hashCode()
        result = 31 * result + PriceId
        result = 31 * result + TimeId
        result = 31 * result + Title.hashCode()
        result = 31 * result + Calorie
        result = 31 * result + numberInCart
        result = 31 * result + Star.hashCode()
        result = 31 * result + TimeValue
        return result
    }
}