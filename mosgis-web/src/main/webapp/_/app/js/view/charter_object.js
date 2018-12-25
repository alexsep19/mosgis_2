define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item ['fias.label'])
        
        fill (view, data.item, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'charter_object_common',   caption: 'Общие'},
                            {id: 'charter_object_working_lists',     caption: 'Перечень услуг'},
                            {id: 'charter_object_docs',     caption: 'Документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_charter_object

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'charter_object.active_tab')
            },

        });
        
        if (data.item ['house.uuid'] && data.item.is_deleted == 0) {
            $(('#house_link')).attr({title: 'Перейти на страницу паспорта дома'})
            clickOn ($('#house_link'), function () { openTab ('/house/' + data.item ['house.uuid']) })
        }

        clickOn ($('#ctr_link'), function () {
        
            openTab ('/charter/' + data.item.uuid_charter)
        
        })

    }

})