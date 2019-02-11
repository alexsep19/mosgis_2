define ([], function () {

    return function (data, view) {
        
        var it = data.item
        
        it.type_label = it.uuid_sr_ctr? 'Договор ресурсоснабжения'
                : it.uuid_contract? 'Договор управления'
                : ''
        
        $('title').text ('Информация о перерывах')
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'interval_common',   caption: 'Общие'},
                            {id: 'interval_objects', caption: 'Объекты жилищного фонда'}
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_interval

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'interval.active_tab')
            },

        });

        if (it['sr_ctr.uuid']) {
            clickOn($('#lnk_sr_ctr'), function () {
                openTab('/supply_resource_contract/' + it['sr_ctr.uuid'])
            })
        }
    }

})