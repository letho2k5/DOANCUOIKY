package com.example.doancuoiky.Domain

import java.io.Serializable

data class FoodModel(
    var BestFood: Boolean = false,
    var CategoryId: String = "",
    var Description: String = "",
    var Id: Int = 0,
    var ImagePath: String = "",
    var LocationId: Int = 0,
    var Price: Double = 0.0,
    var PriceId: Int = 0,
    var TimeId: Int = 0,
    var Title: String = "",
    var Calorie: Int = 0,
    var numberInCart: Int = 0,
    var Star: Double = 0.0,
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