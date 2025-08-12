export type Action = {
    symbol: string;
    value: number;
    thresh: number;
    type: 'buy' | 'sell' | 'missed_buy' | 'missed_sell';
}
