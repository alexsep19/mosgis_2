define ([], function () {
    
    return function (data, view) {

        data.item.status_label = data.vc_house_status[data.item.id_status]

        $('title').text (data.item.label + ', ' + data.item.address)
        
        fill (view, data, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {
                        tabs: [
                            {id: 'living_room_common',   caption: 'Общие'},
                            {id: 'living_room_passport', caption: 'Паспорт'},
                            {id: 'living_room_invalid',  caption: 'Непригодность для проживания', hidden: data.item.f_20131 != 1},
                        ],
                        onClick: $_DO.choose_tab_living_room
                    }                
                
                },
                
            ],
            
            onRender: function (e) {            
                clickActiveTab (this.get ('main').tabs, 'living_room.active_tab')
            },            

        });
        
    }

})