define ([], function () {

    return function (data, view) {
        
        var it = data.item

        $('title').text ('Устав ' + it ['vc_orgs.label'])
        
        if (it ['out_soap.err_text']) {
        
            it.sync_label = 'Ошибка передачи в ГИС ЖКХ. Подробности на вкладке "История изменений"'
            
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
                            {id: 'charter_payments',  caption: 'Услуги управления'},
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