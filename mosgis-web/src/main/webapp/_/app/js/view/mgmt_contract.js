define ([], function () {

    return function (data, view) {
        
        $('title').text ('ДУ №' + data.item.docnum + ' от ' + dt_dmy (data.item.signingdate))
        
        var it = data.item
        
        if (it ['out_soap.err_text']) {
        
            it.sync_label = 'Ошибка передачи в ГИС ЖКХ. Подробности на вкладке "История изменений"'
            
        }
        else if (it.id_ctr_status == 40) {
        
            if (!it.uuid_out_soap) {
            
                it.sync_label = 'Ожидание отправки в ГИС ЖКХ'
            
            }
            else if (!it.contractguid) {
            
                it.sync_label = 'Ожидание ответа от ГИС ЖКХ'
            
            }
        
        }        

        fill (view, it, $('body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'mgmt_contract_common',   caption: 'Общие'},
                            {id: 'mgmt_contract_docs',     caption: 'Документы'},
                            {id: 'mgmt_contract_agreements',     caption: 'Доп. соглашения'},
                            {id: 'mgmt_contract_objects',  caption: 'Объекты управления'},
                            {id: 'mgmt_contract_payments',  caption: 'Услуги управления'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_mgmt_contract

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'mgmt_contract.active_tab')
            },

        });

    }

})