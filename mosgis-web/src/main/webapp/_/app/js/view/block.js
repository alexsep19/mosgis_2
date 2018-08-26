define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item.label + ', ' + data.item.address)
        
        fill (view, data, $('body'))

        $('#container').w2layout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'block_common',   caption: 'Общие'},
                            {id: 'block_passport', caption: 'Паспорт'},
                            {id: 'block_invalid',  caption: 'Непригодность для проживания', hidden: data.item.f_20126 != 1},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_block

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'block.active_tab')
            },

        });
        

    }

})