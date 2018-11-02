define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item.address)        
        
        fill (view, data, $('body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'house_address',  caption: 'Адрес'},
                            {id: 'house_passport', caption: 'Общие'},   // sic
                            {id: 'house_common',   caption: 'Паспорт'}, // sic
                            {id: 'house_constr',   caption: 'Конструктивные элементы', off: !data.item.is_condo},
                            {id: 'house_systems',  caption: 'Внутридомовые сети', off: !data.item.is_condo},
                            {id: 'house_premises', caption: 'Помещения', off: !data.item.is_condo},
                            {id: 'house_living_rooms', caption: 'Комнаты', off: (data.item.is_condo && !data.has_shared_premises_res)},
                            {id: 'house_property_documents', caption: 'Собственники'},
                            {id: 'house_docs',     caption: 'Документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_house

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        

    }

})