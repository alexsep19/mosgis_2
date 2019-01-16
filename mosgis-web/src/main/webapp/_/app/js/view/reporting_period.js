define ([], function () {

    return function (data, view) {
    
        var it = data.item

        it.month_label = w2utils.settings.fullmonths [it.month - 1].toLowerCase ()

        $('title').text ('Выполненные работы за ' + it.month_label + ' ' + it ['plan.year'] + ' / ' + it ['fias.label'])
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'reporting_period_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_reporting_period

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'reporting_period.active_tab')
            },

        });
        
        $(('#obj_link')).attr ({title: 'Перейти на страницу объекта'})
        clickOn ($('#obj_link'), function () {           
            openTab (it ['cao.uuid'] ? '/mgmt_contract_object/' + it ['cao.uuid'] : '/charter_object/' + it ['cho.uuid']) 
        })
        
        clickOn ($('#ca_link'), function () {       
            openTab ('/mgmt_contract/' + it ['ca.uuid'])       
        })
        
        clickOn ($('#ch_link'), function () {       
            openTab ('/charter/' + it ['ch.uuid'])       
        })

    }

})