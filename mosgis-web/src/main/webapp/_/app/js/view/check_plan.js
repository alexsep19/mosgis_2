define ([], function () {

    return function (data, view) {   
        
        fill (view, data, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'check_plan_common', caption: 'Общие'},
                            {id: 'check_plan_examinations', caption: 'Проверки'},
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