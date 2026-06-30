"""Mifflin-St Jeor TDEE calculator."""

ACTIVITY_MULTIPLIERS = {
    "sedentary": 1.2,
    "light": 1.375,
    "moderate": 1.55,
    "very_active": 1.725,
    "extra_active": 1.9,
}

CALORIE_FLOOR = {"male": 1500, "female": 1200, "other": 1350}


def calculate_tdee(
    weight_kg: float,
    height_cm: float,
    age: int,
    sex: str,
    activity_level: str,
    weekly_goal_kg: float = 0.0,
) -> int:
    """Return daily calorie target rounded to nearest 50."""
    sex = sex.lower()

    if sex == "male":
        bmr = 10 * weight_kg + 6.25 * height_cm - 5 * age + 5
    elif sex == "female":
        bmr = 10 * weight_kg + 6.25 * height_cm - 5 * age - 161
    else:
        bmr_m = 10 * weight_kg + 6.25 * height_cm - 5 * age + 5
        bmr_f = 10 * weight_kg + 6.25 * height_cm - 5 * age - 161
        bmr = (bmr_m + bmr_f) / 2

    multiplier = ACTIVITY_MULTIPLIERS.get(activity_level, 1.2)
    tdee = bmr * multiplier

    # 1 kg of fat ≈ 7700 kcal; daily deficit/surplus per week target
    daily_adjustment = weekly_goal_kg * 7700 / 7
    target = tdee + daily_adjustment

    floor = CALORIE_FLOOR.get(sex, 1350)
    target = max(target, floor)

    return int(round(target / 50) * 50)
