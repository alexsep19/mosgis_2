define ([], function () {

    return function (data, view) {
    
        var it = data.item
        
        $('title').text ('Работы / ' + it ['fias.label'])
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'working_plan_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_working_plan

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'working_plan.active_tab')
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