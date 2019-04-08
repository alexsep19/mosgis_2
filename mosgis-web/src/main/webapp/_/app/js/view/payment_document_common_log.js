define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'account_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 11, caption: 'Платёжный документ'},
                {span: 3, caption: 'Запрос в ГИС ЖКХ'},
            ], 
            
            columns: [                

                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор/поставщик', size: 30, render: function (r) {return r ['tb_senders.label'] || r ['vc_users.label']}},
                
                {field: 'debtpreviousperiods', caption: 'Задолженность за предыдущие периоды', size: 20, render: 'float:2'},
                {field: 'advancebllingperiod', caption: 'Аванс на начало расчетного периода, руб.', size: 20, render: 'float:2'},
                {field: 'paymentstaken_dt', caption: 'Дата последнего учтенного платежа',    size: 30, render: _dt},                                
                {field: 'totalpayablebychargeinfo', caption: 'Сумма к оплате за расчетный период по услугам, руб.', size: 20, render: 'float:2'},
                {field: 'totalbypenaltiesandcourtcosts', caption: 'Итого к оплате за расчетный период всего, руб.', size: 20, render: 'float:2'},
                {field: 'totalpayablebypd', caption: 'Итого к оплате за расчетный период всего, руб.', size: 20, render: 'float:2'},
                {field: 'subsidiescompensation_', caption: 'Субсидии, компенсации и иные меры соц. поддержки граждан, руб.', size: 20, render: 'float:2'},
                {field: 'totalpayablebypdwith_da', caption: 'Итого к оплате за расчетный период c учетом задолженности/переплаты, руб.', size: 20, render: 'float:2'},
                {field: 'dateoflastreceivedpayment', caption: 'Дата последней поступившей оплаты',    size: 30, render: _dt},
                {field: 'paidcash', caption: 'Оплачено денежных средств, руб.', size: 20, render: 'float:2'},
                {field: 'limitindex', caption: 'Предельный (максимальный) индекс изменения размера платы граждан за коммунальные услуги в муниципальном образовании, %', size: 20, render: 'float:2'},                
                
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],
            
            url: '/_back/?type=payment_documents&part=log&id=' + $_REQUEST.id,            
            
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                switch (c.field) {
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r.uuid_message) return openTab ('/out_soap_rp/' + r.uuid_message)
                }
            
            }

        }).refresh ();

    }

})