define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item.label + ', ' + data.item.address)
        
        fill (view, data, $('body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'premise_residental_common',   caption: 'Общие'},
                            {id: 'premise_residental_passport', caption: 'Паспорт'},
                            {id: 'premise_residental_invalid',  caption: 'Непригодность для проживания', hidden: data.item.f_20126 != 1},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_premise_residental

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'premise_residental.active_tab')
            },            

        })        

    }

})