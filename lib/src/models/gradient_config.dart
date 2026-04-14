class GradientConfig {
  final String type;
  final List<String> colors;
  final List<double>? stops;
  final Map<String, double>? begin;
  final Map<String, double>? end;
  final Map<String, double>? center;
  final double? radius;

  const GradientConfig({
    this.type = 'linear',
    required this.colors,
    this.stops,
    this.begin = const {'x': 0.5, 'y': 0.0},
    this.end = const {'x': 0.5, 'y': 1.0},
    this.center = const {'x': 0.5, 'y': 0.3},
    this.radius = 0.8,
  });

  Map<String, dynamic> toMap() {
    return {
      'type': type,
      'colors': colors,
      if (stops != null) 'stops': stops,
      if (begin != null) 'begin': begin,
      if (end != null) 'end': end,
      if (center != null) 'center': center,
      if (radius != null) 'radius': radius,
    };
  }

  factory GradientConfig.fromMap(Map<String, dynamic> map) {
    return GradientConfig(
      type: map['type'] as String? ?? 'linear',
      colors: (map['colors'] as List).cast<String>(),
      stops: (map['stops'] as List?)?.cast<double>(),
      begin: (map['begin'] as Map?)?.cast<String, double>(),
      end: (map['end'] as Map?)?.cast<String, double>(),
      center: (map['center'] as Map?)?.cast<String, double>(),
      radius: (map['radius'] as num?)?.toDouble(),
    );
  }

  GradientConfig copyWith({
    String? type,
    List<String>? colors,
    List<double>? stops,
    Map<String, double>? begin,
    Map<String, double>? end,
    Map<String, double>? center,
    double? radius,
  }) {
    return GradientConfig(
      type: type ?? this.type,
      colors: colors ?? this.colors,
      stops: stops ?? this.stops,
      begin: begin ?? this.begin,
      end: end ?? this.end,
      center: center ?? this.center,
      radius: radius ?? this.radius,
    );
  }
}
