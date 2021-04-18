import 'package:cordatradeclient/models/trade_state_model.dart';
import 'package:equatable/equatable.dart';

abstract class HomeState extends Equatable
{
  @override
  List<Object?> get props => [];
}

class HomeLoadingState extends HomeState
{
}

class HomeLoadedState extends HomeState
{
  final String me;
  final List<String>? peers;
  final String? selectedPeer;
  final List<TradeStateModel>? trades;
  final TradeStateModel? selectedTrade;

  HomeLoadedState(this.me, this.peers, this.trades,
      {this.selectedPeer, this.selectedTrade});

  HomeLoadedState setSelectedPeer(String selectedPeer) {
    return HomeLoadedState(me, peers, trades, selectedPeer: selectedPeer);
  }

  HomeLoadedState setSelectedTrade(TradeStateModel selectedTrade) {
    return HomeLoadedState(me, peers, trades,
        selectedPeer: selectedPeer, selectedTrade: selectedTrade);
  }

  @override
  List<Object?> get props => [me, peers, trades, selectedPeer, selectedTrade];
}