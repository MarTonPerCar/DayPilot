class AppTechRestriction {
  const AppTechRestriction({
    required this.appPackage,
    required this.appName,
    required this.limitMinutes,
    required this.isActive,
  });

  final String appPackage;
  final String appName;
  final int limitMinutes;
  final bool isActive;
}
