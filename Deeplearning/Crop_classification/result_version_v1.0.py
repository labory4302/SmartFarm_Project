# 0_1. 사용할 패키지 불러오기
import keras
import numpy as np
import cv2
from keras.models import load_model
from keras.preprocessing.image import ImageDataGenerator

# 0_2. 전역변수 및 상대 경로 지정
#--------------------------------------------------------------
roadimg_folder_src = './result_testimg_01'
roadimg_src= './result_testimg_01/img/'
roadimg_name = 'resultimg_01.jpg'

vegetable_classnumber = 0
result_disease = 0
vegetable_class = ["배추", "고추", "깨"]
result_class = []


# 가장 높은 확률의 학습 데이터 인덱스 찾기
def find_index(output):
    learning_pre_result = 0
    vegetable_classnumber = 0
    
    for i in range(len(output[0])):
        if learning_pre_result < output[0][i]:
            learning_pre_result = output[0][i]
            vegetable_classnumber = i
            
    return vegetable_classnumber




# 1. 영상 불러오기
#--------------------------------------------------------------
# ImageDataGenerator 형식의 데이터 틀
test_image = ImageDataGenerator(rescale=1./255)

# .flow_from_directory 함수를 통해 폴더 안에 이미지 호출
test_generator = test_image.flow_from_directory(
        roadimg_folder_src,
        target_size=(30, 30),
        class_mode='categorical')

# 2. 모델 불러오기
#--------------------------------------------------------------
# 각 러닝된 모델 불러오기
model_vegetable = load_model('test_vegetable_class_02.h5')

#model_cabbage = load_model('test_cabbage_class_01.h5')
#model_pepper = load_model('test_pepper_class_01.h5')
#model_sesame = load_model('test_sesame_class_01.h5')

# 각 작물에 대한 질병 학습 모델을 배열로 저장
vegetable_model_class = ['', '', '']
vegetable_model_class[0] = load_model('test_cabbage_class_01.h5')
vegetable_model_class[1] = load_model('test_pepper_class_01.h5')
vegetable_model_class[2] = load_model('test_sesame_class_01.h5')

# 작물 종류에 대한 학습 모델에게 이미지 판단(0:배추, 1:고추, 2:깨)
output = model_vegetable.predict_generator(test_generator,steps=1)

# 작물 종류 분류
vegetable_classnumber = find_index(output)

# 0번 인덱스인 배추의 질병 종류 판단
if vegetable_classnumber == 0:
    
    result_output = vegetable_model_class[0].predict_generator(test_generator, steps=1)
    result_disease = find_index(result_output)
    result_class = ["뿌리 혹병", "배추 역병", "무름병"]

# 1번 인덱스인 고추의 질병 종류 판단
elif vegetable_classnumber == 1:
    result_output = vegetable_model_class[1].predict_generator(test_generator,steps=1)
    result_disease = find_index(result_output)
    result_class = ["탄저병", "칼슘부족", "고추 역병"]

# 2번 인덱스인 깨의 질병 종류 판단
elif vegetable_classnumber == 2:
    result_output = vegetable_model_class[2].predict_generator(test_generator,steps=1)
    result_disease = find_index(result_output)
    result_class = ["마름병", "참깨 역병", "흰가루병"]

# 해당 이미지 불러와 출력
view_image = cv2.imread(roadimg_src + roadimg_name)
cv2.imshow("result", view_image)

# 학습 모델의 최종 병해 판단 결과 출력
print("작물: %s, %s: %.2f%%" %(vegetable_class[vegetable_classnumber], result_class[result_disease], result_output[0][result_disease]*100))


    
    
                     
                        
