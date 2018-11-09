define ([], function () {

    return function (data, view) {
    
        var it = data.item
        
        $('title').text ('Работы / ' + it ['fias.label'])
        
        fill (view, it, $('body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'working_list_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_working_list

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'working_list.active_tab')
            },

        });
        
        $(('#obj_link')).attr ({title: 'Перейти на страницу объекта'})
        clickOn ($('#obj_link'), function () {           
            openTab (it.uuid_contract_object ? '/mgmt_contract_object/' + it.uuid_contract_object : '/charter_object/' + it.uuid_charter_object) 
        })
        
        clickOn ($('#ca_link'), function () {       
            openTab ('/mgmt_contract/' + it ['ca.uuid'])       
        })
        
        clickOn ($('#ch_link'), function () {       
            openTab ('/charter/' + it ['ch.uuid'])       
        })

    }

})