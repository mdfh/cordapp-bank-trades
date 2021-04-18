import 'package:cordatradeclient/models/trade_state_model.dart';
import 'package:equatable/equatable.dart';

abstract class TradeDetailState extends Equatable {
  @override
  List<Object?> get props => [];
}

class TradeDetailLoadingState extends TradeDetailState {}

class TradeDetailErrorState extends TradeDetailState {}

class TradeDetailLoadedState extends TradeDetailState {
  final TradeStateModel model;
  final bool isLoading;

  TradeDetailLoadedState(this.model, {this.isLoading = false});

  TradeDetailLoadedState setLoading(bool isLoading)
  {
    return TradeDetailLoadedState(this.model, isLoading: isLoading);
  }

  @override
  List<Object?> get props => [model, isLoading];
}
