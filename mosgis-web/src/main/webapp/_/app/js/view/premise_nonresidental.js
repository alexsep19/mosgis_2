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
                            {id: 'premise_nonresidental_common',   caption: 'Общие'},
                            {id: 'premise_nonresidental_passport', caption: 'Паспорт'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_premise_nonresidental

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        

    }

})