define ([], function () {

    return function (data, view) {
        
        var it = data.item

        $('title').text ('Устав ' + it ['vc_orgs.label'])
        
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
                            {id: 'charter_common',   caption: 'Общие'},
                            {id: 'charter_docs',     caption: 'Документы'},
                            {id: 'charter_objects',  caption: 'Объекты управления'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_charter

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'charter.active_tab')
            },

        });

    }

})