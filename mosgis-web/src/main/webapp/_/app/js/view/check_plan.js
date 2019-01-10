define ([], function () {

    return function (data, view) {
        
        //$('title').text (data.item.address)        
        
        fill (view, data, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'check_plan_common', caption: 'Общие'},
                        ],

                        onClick: $_DO.choose_tab_check_plan

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        

    }

})